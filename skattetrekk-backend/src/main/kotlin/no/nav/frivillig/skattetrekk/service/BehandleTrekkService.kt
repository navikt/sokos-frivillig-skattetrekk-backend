package no.nav.frivillig.skattetrekk.service

import no.nav.frivillig.skattetrekk.client.trekk.TrekkClient
import no.nav.frivillig.skattetrekk.client.trekk.api.*
import no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy.Kilde
import no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy.OpphorAndreTrekkRequest
import no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy.OpprettAndreTrekkRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class BehandleTrekkService(
    private val trekkClient: TrekkClient,
    private val geografiskLokasjonService: GeografiskLokasjonService
) {

    private val log = LoggerFactory.getLogger(BehandleTrekkService::class.java)

    fun behandleTrekk(pid: String, tilleggstrekk: Int, satsType: SatsType) {

        val finnTrekkListe = trekkClient.finnTrekkListe(pid, TrekkTypeCode.FSKT)
        val trekkvedtakId = finnTrekkListe?.sortedByDescending { it.trekkperiodeFom }?.firstOrNull()?.trekkvedtakId

        if (trekkvedtakId != null && tilleggstrekk == 0) {
            log.info("Opphører trekk = $trekkvedtakId")
            opphoerTrekk(pid, trekkvedtakId)
        } else if (trekkvedtakId != null && trekkvedtakId > 0) {
            log.info("Oppdaterer trekk = $trekkvedtakId")
            oppdaterTrekk(pid, trekkvedtakId, tilleggstrekk, satsType)
        } else {
            log.info("Oppretter nytt trekk")
            opprettTrekk(pid, tilleggstrekk, satsType)
        }
    }

    fun opprettTrekk(pid: String, tilleggstrekk: Int, satsType: SatsType): Long? {

        log.info("Henter skatt og trekk")

        if (skalOppretteNyttTrekk(tilleggstrekk, null)) {
            val trekkalternativKode = if (satsType == SatsType.KRONER)
                TrekkalternativKode.LOPM else TrekkalternativKode.LOPP

            val brukersNavEnhet = geografiskLokasjonService.hentNavEnhet(pid)

            log.info("Oppretter nytt frivillig skattetrekk")
            val trekkOpprettet = trekkClient.opprettAndreTrekk(
                pid,
                OpprettAndreTrekkRequest(
                    Kilde.PPO1.name,
                    opprettNyttTrekkRequest(pid, tilleggstrekk, trekkalternativKode.name, brukersNavEnhet, LocalDate.now().withDayOfMonth(1))
                ))

            return trekkOpprettet?.trekkvedtakId
        }

        return null
    }

    fun oppdaterTrekk(pid: String, trekkvedtakId: Long, tilleggstrekk: Int, satsType: SatsType) {

        log.info("Henter skatt og trekk for vedtakId=$trekkvedtakId")
        val andreTrekk = trekkClient.hentSkattOgTrekk(pid, trekkvedtakId)
            ?.andreTrekk

        val sorterteSatsperioder = andreTrekk
            ?.satsperiodeListe
            ?.sortedBy { it.fom } ?: emptyList()

        val lopendeSatsperioder = sorterteSatsperioder.filter { isLopende(it) }
        val fremtidigeSatsperioder = sorterteSatsperioder.filter { isFremtidig(it) }

        if (lopendeSatsperioder.isNotEmpty() || fremtidigeSatsperioder.isNotEmpty()) {

            // sjekk har nytt fremtidig trekk, dvs tilleggstrekket er > 0
            if (skalOppretteNyttTrekk(tilleggstrekk, andreTrekk)) {
                val trekkalternativKode = if(satsType == SatsType.KRONER) TrekkalternativKode.LOPM else TrekkalternativKode.LOPP
                val brukersNavEnhet = geografiskLokasjonService.hentNavEnhet(pid)

                val nesteMaaned = LocalDate.now().plusMonths(1L).withDayOfMonth(1)

                log.info("Opphører eksisterende trekk")
                opphorTrekk(pid, trekkvedtakId, nesteMaaned)

                log.info("Oppretter nytt frivillig skattetrekk")
                trekkClient.opprettAndreTrekk(
                    pid,
                    OpprettAndreTrekkRequest(
                        Kilde.PPO1.name,
                        opprettNyttTrekkRequest(pid, tilleggstrekk, trekkalternativKode.name, brukersNavEnhet,nesteMaaned)
                    ))
            }
        }

    }

    fun opphoerTrekk(pid: String, trekkvedtakId: Long) {

        log.info("Henter skattetrekk")
        val sorterteSatsperioder = trekkClient.hentSkattOgTrekk(pid, trekkvedtakId)
            ?.andreTrekk
            ?.satsperiodeListe
            ?.sortedBy { it.fom } ?: emptyList()

        val lopendeSatsperioder = sorterteSatsperioder.filter { isLopende(it) }
        val fremtidigeSatsperioder = sorterteSatsperioder.filter { isFremtidig(it) }

        val forsteDagNesteMaaned = LocalDate.now().plusMonths(1L).withDayOfMonth(1)
        // Opphør løpende trekk, om det finnes
        if (fremtidigeSatsperioder.isNotEmpty() || lopendeSatsperioder.isNotEmpty()) {
            log.info("Opphører løpende trekk")
            opphorTrekk(pid, trekkvedtakId, forsteDagNesteMaaned)
        }
    }

    private fun skalOppretteNyttTrekk(tilleggstrekk: Int, andreTrekk: AndreTrekkResponse?): Boolean {
        if (andreTrekk == null) {
            return tilleggstrekk > 0
        } else if (andreTrekk != null && tilleggstrekk == 0) {
            return false
        }

        val finnesIkkeLopende = andreTrekk?.satsperiodeListe?.find { isLopende(it) } == null
        val finnesIkkeFremtidigTrekk = andreTrekk?.satsperiodeListe?.find { isFremtidig(it) } == null

        return finnesIkkeLopende || finnesIkkeFremtidigTrekk
    }

    private fun opprettNyttTrekkRequest(pid: String, tilleggstrekk: Int, trekkalternativKode: String, brukersNavEnhet: String, date: LocalDate): AndreTrekkRequest =
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
            satsperiodeListe = listOf(opprettStatsperiode(tilleggstrekk.toDouble(), date)),
        )

    private fun lagOppdaterTrekkRequest(
        pid: String,
        satsType: SatsType,
        tilleggstrekk: Double,
        andreTrekk: AndreTrekkResponse
    ): AndreTrekkRequest {
        val satsperioder = (andreTrekk?.satsperiodeListe?.toMutableList() ?: mutableListOf()).plus(opprettStatsperiode(tilleggstrekk, LocalDate.now()))
        val fagomradeListe = andreTrekk?.fagomradeListe ?: listOf(
            Fagomrade(
                trekkgruppeKode = "PENA",
                fagomradeKode = null,
                erFeilregistrert = null
            )
        )

        val ansvarligEnhetId = andreTrekk?.ansvarligEnhetId ?: geografiskLokasjonService.hentNavEnhet(pid)
        return AndreTrekkRequest(
            ansvarligEnhetId = ansvarligEnhetId,
            belopSaldotrekk = andreTrekk.belopSaldotrekk,
            debitorOffnr = pid,
            datoOppfolging = andreTrekk.datoOppfolging,
            gyldigTom = andreTrekk.gyldigTom,
            trekktypeKode = TrekkTypeCode.FRIS.name,
            trekkalternativKode = if (satsType == SatsType.KRONER) TrekkalternativKode.LOPM.name else TrekkalternativKode.LOPP.name,
            tssEksternId = andreTrekk.tssEksternId,
            kreditorKid = andreTrekk.kreditorKid,
            kreditorRef = andreTrekk.kreditorRef,
            prioritetFom = andreTrekk.prioritetFom,
            fagomradeListe = fagomradeListe,
            satsperiodeListe = satsperioder?.toList()!!,
        )
    }

    fun opprettStatsperiode(tilleggstrekk: Double, today: LocalDate): Satsperiode {
        val forsteDatoNesteMaaned = today.withDayOfMonth(1)
        val sisteDagDetteAret = LocalDate.of(today.year, 12, 31)
        return Satsperiode(
            fom = forsteDatoNesteMaaned,
            tom = sisteDagDetteAret,
            sats = tilleggstrekk.toBigDecimal()
        )
    }

    fun opphorTrekk(pid: String, trekkvedtakId: Long, nesteMaaned: LocalDate) {
        val opphorAndreTrekkRequest = OpphorAndreTrekkRequest(kilde = Kilde.PPO1.name, trekkvedtakId, nesteMaaned)
        trekkClient.opphorAndreTrekk(pid, opphorAndreTrekkRequest)
    }

    fun isFremtidig(satsperiode: Satsperiode): Boolean {
        if (!satsperiode.erFeilregistrert!!) {
            val fom = satsperiode.fom
            val today = LocalDate.now()
            return today.isBefore(fom)
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