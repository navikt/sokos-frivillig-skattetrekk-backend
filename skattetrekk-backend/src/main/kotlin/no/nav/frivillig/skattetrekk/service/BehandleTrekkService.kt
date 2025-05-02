package no.nav.frivillig.skattetrekk.service

import no.nav.frivillig.skattetrekk.client.trekk.TrekkClient
import no.nav.frivillig.skattetrekk.client.trekk.api.*
import no.nav.frivillig.skattetrekk.security.SecurityContextUtil
import no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy.Kilde
import no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy.OppdaterAndreTrekkRequest
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
                    opprettNyttTrekkRequest(pid, tilleggstrekk, trekkalternativKode.name, brukersNavEnhet)
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

        // Opphør løpende trekk, om det finnes
        if (trekkvedtakId != null && lopendeSatsperioder.isNotEmpty()) {
            log.info("Opphører løpende trekk")
            opphorLoependeTrekk(pid, trekkvedtakId)
        }

        // Opphør fremtidige trekk om det finnes
        if (trekkvedtakId != null && fremtidigeSatsperioder.isNotEmpty()) {
            log.info("Opphører fremtidige trekk")
            opphorFremtidigeTrekk(pid, trekkvedtakId)
        }

        // sjekk har nytt fremtidig trekk, dvs tilleggstrekket er > 0
        if (skalOppretteNyttTrekk(tilleggstrekk, andreTrekk)) {
            val trekkalternativKode = if(satsType == SatsType.KRONER) TrekkalternativKode.LOPM else TrekkalternativKode.LOPP
            val brukersNavEnhet = geografiskLokasjonService.hentNavEnhet(pid)

            log.info("Oppretter nytt frivillig skattetrekk")
            trekkClient.opprettAndreTrekk(
                pid,
                OpprettAndreTrekkRequest(
                    Kilde.PPO1.name,
                    opprettNyttTrekkRequest(pid, tilleggstrekk, trekkalternativKode.name, brukersNavEnhet)
                ))
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

        // Opphør løpende trekk, om det finnes
        if (lopendeSatsperioder.isNotEmpty()) {
            log.info("Opphører løpende trekk")
            opphorLoependeTrekk(pid, trekkvedtakId)
        }

        // Opphør fremtidige trekk om det finnes
        if (fremtidigeSatsperioder.isNotEmpty()) {
            log.info("Opphører fremtidige trekk")
            opphorFremtidigeTrekk(pid, trekkvedtakId)
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

    private fun opprettNyttTrekkRequest(pid: String, tilleggstrekk: Int, trekkalternativKode: String, brukersNavEnhet: String): AndreTrekkRequest =
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
            satsperiodeListe = listOf(opprettStatsperiode(tilleggstrekk.toDouble(), LocalDate.now())),
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
        val forsteDatoNesteMaaned = today.plusMonths(1L).withDayOfMonth(1)
        val sisteDagDetteAret = LocalDate.of(today.year, 12, 31)
        return Satsperiode(
            fom = forsteDatoNesteMaaned,
            tom = sisteDagDetteAret,
            sats = tilleggstrekk.toBigDecimal()
        )
    }

    fun opphorLoependeTrekk(pid: String, trekkvedtakId: Long) {
        val forsteDagNesteMaaned = LocalDate.now().plusMonths(1L).withDayOfMonth(1)
        val opphorAndreTrekkRequest = OpphorAndreTrekkRequest(kilde = Kilde.PPO1.name, trekkvedtakId, forsteDagNesteMaaned)
        trekkClient.opphorAndreTrekk(pid, opphorAndreTrekkRequest)
    }

    fun opphorFremtidigeTrekk(pid: String, trekkvedtakId: Long) {
        val forsteDagNesteMaaned = LocalDate.now().plusMonths(1L).withDayOfMonth(1)
        val opphorAndreTrekkRequest = OpphorAndreTrekkRequest(kilde = Kilde.PPO1.name, trekkvedtakId, forsteDagNesteMaaned)
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