package no.nav.frivillig.skattetrekk.service

import no.nav.frivillig.skattetrekk.client.trekk.TrekkClient
import no.nav.frivillig.skattetrekk.client.trekk.api.*
import no.nav.frivillig.skattetrekk.util.isDateInPeriod
import no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy.Kilde
import no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy.OppdaterAndreTrekkRequest
import no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy.OpphorAndreTrekkRequest
import no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy.OpprettAndreTrekkRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class BehandleTrekkService(
    private val trekkClient: TrekkClient
) {

    private val log = LoggerFactory.getLogger(BehandleTrekkService::class.java)
    private val ANSVARLIG_ENHET = "4819"

    fun behandleTrekk(pid: String, tilleggstrekk: Int, satsType: SatsType) {

        val virkningsdato = LocalDate.now().plusMonths(1L).withDayOfMonth(1)

        val frivilligeSkattetrekk = trekkClient.finnTrekkListe(pid, TrekkTypeCode.FRIS)
        val lopendeTilleggstrekk = frivilligeSkattetrekk.findLopendeTrekk()
        val nesteTilleggstrekk = frivilligeSkattetrekk.nesteTrekkPeriode()

        if (tilleggstrekk == 0) {

            // Opphør løpende trekk
            lopendeTilleggstrekk?.let {
                if(it.fortsetterEtterVirkningsdato(virkningsdato)) {
                    opphoerTrekk(pid, it.trekkvedtakId!!)
                }
            }

            // Opphør fremtidig trekk
            nesteTilleggstrekk?.let { opphoerTrekk(pid, it.trekkvedtakId!!) }
        } else if (lopendeTilleggstrekk == null && nesteTilleggstrekk == null) {
            opprettTrekk(pid, tilleggstrekk, satsType, virkningsdato)
        } else {
            if (lopendeTilleggstrekk?.fortsetterEtterVirkningsdato(virkningsdato) == true) {
                // Kan ikke oppdatere et krone trekk med prosentrekk fordi da feiler oppdrag med følgende melding:
                // Ukjent feilkode B725006F feil var Prosenttrekk kan ikke overstige 100%
                if (skalOppdatereSammeTrekkType(satsType, TrekkalternativKode.valueOf(lopendeTilleggstrekk.trekkalternativ?.kode!!))) {
                    oppdaterTrekk(pid, lopendeTilleggstrekk.trekkvedtakId!!, tilleggstrekk, satsType)
                } else {
                    opphoerTrekk(pid, lopendeTilleggstrekk.trekkvedtakId!!)
                    opprettTrekk(pid, tilleggstrekk, satsType, virkningsdato)
                }
            } else {
                if (skalOppdatereSammeTrekkType(satsType, TrekkalternativKode.valueOf(nesteTilleggstrekk?.trekkalternativ?.kode!!))) {
                    oppdaterTrekk(pid, nesteTilleggstrekk.trekkvedtakId!!, tilleggstrekk, satsType)
                } else {
                    opphoerTrekk(pid, nesteTilleggstrekk.trekkvedtakId!!)
                    opprettTrekk(pid, tilleggstrekk, satsType, virkningsdato)
                }
            }
        }
    }

    private fun skalOppdatereSammeTrekkType(satsType: SatsType, trekkalternativKode: TrekkalternativKode): Boolean {
        return when (satsType) {
            SatsType.KRONER -> trekkalternativKode == TrekkalternativKode.LOPM
            SatsType.PROSENT -> trekkalternativKode == TrekkalternativKode.LOPP
        }
    }

    private fun TrekkInfo.fortsetterEtterVirkningsdato(virkningsdato: LocalDate) = this.trekkperiodeTom?.isAfter(virkningsdato) == true
    private fun List<TrekkInfo>.findLopendeTrekk() = this.find { isDateInPeriod(LocalDate.now(), it.trekkperiodeFom, it.trekkperiodeTom) }
    private fun List<TrekkInfo>.nesteTrekkPeriode() = this.find { isStartingFirstOfNextMonth(it) }


    private fun isStartingFirstOfNextMonth(trekkInfo: TrekkInfo): Boolean {
        val fom = trekkInfo.trekkperiodeFom
        val firstOfNextMonth = LocalDate.now().plusMonths(1L).withDayOfMonth(1)
        return fom == firstOfNextMonth
    }

    fun opprettTrekk(pid: String, tilleggstrekk: Int, satsType: SatsType, gjelderFraOgMed: LocalDate): Long? {

        log.info("Henter skatt og trekk")

        if (skalOppretteNyttTrekk(tilleggstrekk, null)) {
            val trekkalternativKode = if (satsType == SatsType.KRONER)
                TrekkalternativKode.LOPM else TrekkalternativKode.LOPP

            log.info("Oppretter nytt frivillig skattetrekk")
            val trekkOpprettet = trekkClient.opprettAndreTrekk(
                pid,
                OpprettAndreTrekkRequest(
                    Kilde.PPO1.name,
                    opprettNyttTrekkRequest(pid, tilleggstrekk, trekkalternativKode.name, ANSVARLIG_ENHET, gjelderFraOgMed)
                ))

            return trekkOpprettet?.trekkvedtakId
        }

        return null
    }


    fun oppdaterTrekk(pid: String, trekkvedtakId: Long, tilleggstrekk: Int, satsType: SatsType) {
        val trekkalternativKode = if (satsType == SatsType.KRONER) TrekkalternativKode.LOPM else TrekkalternativKode.LOPP
        val andreTrekkResponse = trekkClient.hentSkattOgTrekk(pid, trekkvedtakId)?.andreTrekk
        if (andreTrekkResponse != null) {
            val sorterteSatsperioder = andreTrekkResponse.satsperiodeListe?.sortedBy { it.fom } ?: emptyList()
            val nySatsperiode = opprettStatsperiode(tilleggstrekk, LocalDate.now().plusMonths(1).withDayOfMonth(1))
            val oppdaterteSatsperioder = oppdaterSatsperioder(LocalDate.now(), sorterteSatsperioder, nySatsperiode)

            val andreTrekkRequest = AndreTrekkRequest(
                ansvarligEnhetId = andreTrekkResponse.ansvarligEnhetId!!,
                belopSaldotrekk = andreTrekkResponse.belopSaldotrekk,
                datoOppfolging = andreTrekkResponse.datoOppfolging,
                gyldigTom = andreTrekkResponse.prioritetFom,
                debitorOffnr = andreTrekkResponse.debitor?.id!!,
                trekkalternativKode = trekkalternativKode.name,
                trekktypeKode = andreTrekkResponse.trekktype?.kode!!,
                tssEksternId = andreTrekkResponse.tssEksternId,
                kreditorKid = andreTrekkResponse.kreditor?.id,
                kreditorRef = andreTrekkResponse.kreditorRef,
                prioritetFom = andreTrekkResponse.prioritetFom,
                satsperiodeListe = oppdaterteSatsperioder,
                fagomradeListe = andreTrekkResponse.fagomradeListe?.map { Fagomrade(null, it.kode, null) },
            )

            val oppdaterAndreTrekkRequest = OppdaterAndreTrekkRequest(
                trekkvedtakId = trekkvedtakId, andreTrekk = andreTrekkRequest, kilde = Kilde.PPO1.name)
            trekkClient.oppdaterAndreTrekk(pid, oppdaterAndreTrekkRequest)
        }
    }

    fun opphoerTrekk(pid: String, trekkvedtakId: Long) {

        log.info("Henter skattetrekk")
        val sorterteSatsperioder = trekkClient.hentSkattOgTrekk(pid, trekkvedtakId)
            ?.andreTrekk
            ?.satsperiodeListe
            ?.sortedBy { it.fom } ?: emptyList()

        val lopendeSatsperioder = sorterteSatsperioder.filter { isLopende(it) }
        val fremtidigeSatsperioder = sorterteSatsperioder.filter { isFremtidig(LocalDate.now(), it) }

        val opphorDato = LocalDate.now().plusMonths(1L).withDayOfMonth(1)
        // Opphør løpende trekk, om det finnes
        log.info("Opphører løpende trekk")
        opphorTrekk(pid, trekkvedtakId, opphorDato)
    }

    private fun skalOppretteNyttTrekk(tilleggstrekk: Int, andreTrekk: AndreTrekkResponse?): Boolean {
        if (andreTrekk == null) {
            return tilleggstrekk > 0
        } else if (tilleggstrekk == 0) {
            return false
        }

        val finnesIkkeLopende = andreTrekk.satsperiodeListe?.find { isLopende(it) } == null
        val finnesIkkeFremtidigTrekk = andreTrekk.satsperiodeListe?.find { isFremtidig(LocalDate.now(), it) } == null

        return finnesIkkeLopende || finnesIkkeFremtidigTrekk
    }

    private fun opprettNyttTrekkRequest(pid: String, tilleggstrekk: Int, trekkalternativKode: String, brukersNavEnhet: String, trekkGjelderFraOgMed: LocalDate): AndreTrekkRequest =
        AndreTrekkRequest(
            ansvarligEnhetId = brukersNavEnhet,
            debitorOffnr = pid,
            trekktypeKode = TrekkTypeCode.FRIS.name,
            trekkalternativKode = trekkalternativKode,
            fagomradeListe = listOf(
                Fagomrade(
                    trekkgruppeKode = "PENA",
                    fagomradeKode = null,
                    erFeilregistrert = null
                )
            ),
            satsperiodeListe = listOf(opprettStatsperiode(tilleggstrekk, trekkGjelderFraOgMed)),
        )

    fun opprettStatsperiode(tilleggstrekk: Int, gjelderFom: LocalDate): Satsperiode {
        val sisteDagDetteAret = LocalDate.of(gjelderFom.year, 12, 31)
        return Satsperiode(
            fom = gjelderFom,
            tom = sisteDagDetteAret,
            sats = tilleggstrekk.toBigDecimal(),
            erFeilregistrert = false,
            sporing = Sporing(
                opprettetInfo = Sporingsdetalj(
                    kilde = Kilde.PPO1.name,
                    opprettetAvId = "system",
                    opprettetDato = LocalDate.now()
                )
            )
        )
    }

    fun oppdaterSatsperioder(today: LocalDate, eksisterendeSatsperioder: List<Satsperiode>, nySatsperiode: Satsperiode): List<Satsperiode> {
        val oppdaterSatsperioder = eksisterendeSatsperioder.toMutableList()

        eksisterendeSatsperioder.forEach {
            if (isFremtidig(today,it)) {
                oppdaterSatsperioder.remove(it) // Fjerner fremtidig satsperiode
            } else if (it.tom == null || it.tom.isAfter(nySatsperiode.fom)) {
                oppdaterSatsperioder.remove(it) // Fjerner løpende satsperiode, da den oppdateres med ny
                val oppdatertSisteEksisterendeSatsperiode = it.copy(tom = nySatsperiode.fom?.minusDays(1))
                oppdaterSatsperioder.add(oppdatertSisteEksisterendeSatsperiode)
            }
        }

        oppdaterSatsperioder.add(nySatsperiode)
        return oppdaterSatsperioder
    }

    fun opphorTrekk(pid: String, trekkvedtakId: Long, opphorFom: LocalDate) {
        val opphorAndreTrekkRequest = OpphorAndreTrekkRequest(kilde = Kilde.PPO1.name, trekkvedtakId, opphorFom)
        trekkClient.opphorAndreTrekk(pid, opphorAndreTrekkRequest)
    }

    fun isFremtidig(gjeldendeDato: LocalDate, satsperiode: Satsperiode): Boolean {
        if (!satsperiode.erFeilregistrert!!) {
            val fom = satsperiode.fom
            return gjeldendeDato.isBefore(fom)
        }

        return false
    }

    fun isLopende(satsperiode: Satsperiode): Boolean {
        if (satsperiode.erFeilregistrert == false) {
            val fom = satsperiode.fom
            val tom = satsperiode.tom
            val today = LocalDate.now()
            return today == fom || today == tom  || today.isAfter(fom) && today.isBefore(tom)
        }
        return false
    }

}