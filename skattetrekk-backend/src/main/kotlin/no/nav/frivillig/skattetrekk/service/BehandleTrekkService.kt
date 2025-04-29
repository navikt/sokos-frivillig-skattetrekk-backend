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

    fun behandleTrekk(trekkvedtakId: Long?, verdi: Int, satsType: SatsType) {
        log.info("Bruker har valgt trekk i $satsType")

        val pid = SecurityContextUtil.getPidFromContext()

        val andreTrekk = if (trekkvedtakId != null) trekkClient.hentSkattOgTrekk(pid, trekkvedtakId)?.andreTrekk else null
        val sorterteSatsperioder = andreTrekk?.satsperiodeListe?.sortedBy { it.fom } ?: emptyList()

        val lopendeSatsperioder = sorterteSatsperioder.filter { isLopende(it) }
        val fremtidigeSatsperioder = sorterteSatsperioder.filter { isFremtidig(it) }

        val tilleggstrekk: Double = verdi.toDouble()

        // Opphør løpende trekk, om det finnes
        if (trekkvedtakId != null && lopendeSatsperioder.isNotEmpty()) {
            opphorLoependeTrekk(pid, trekkvedtakId)
        }

        // Opphør fremtidige trekk om det finnes
        if (trekkvedtakId != null && fremtidigeSatsperioder.isNotEmpty()) {
            opphorFremtidigeTrekk(pid, trekkvedtakId)
        }

        // sjekk har nytt fremtidig trekk, dvs tilleggstrekket er > 0
        if (skalOppretteNyttTrekk(tilleggstrekk, andreTrekk)) {
            val trekkalternativKode = if(satsType == SatsType.KRONER) TrekkalternativKode.LOPM else TrekkalternativKode.LOPP
            val brukersNavEnhet = geografiskLokasjonService.hentNavEnhet(pid)

            trekkClient.opprettAndreTrekk(
                pid,
                OpprettAndreTrekkRequest(
                    Kilde.PPO1.name,
                    opprettNyttTrekkRequest(pid, tilleggstrekk, trekkalternativKode.name, brukersNavEnhet)
                ))
        }

        // Sjekk om trekk skal oppdateres
        if(skalOppdatereTrekk(andreTrekk)) {
            trekkClient.oppdaterAndreTrekk(pid, OppdaterAndreTrekkRequest(
                andreTrekk?.trekkvedtakId!!,
                lagOppdaterTrekkRequest(pid, satsType, tilleggstrekk, andreTrekk),
                Kilde.PPO1.name,
            ))
        }

    }

    fun oppdaterTrekk(trekkvedtakId: Long?, verdi: Int, satsType: SatsType) {

    }

    fun opphoerTrekk(trekkvedtakId: Long) {

        val pid = SecurityContextUtil.getPidFromContext()

        val andreTrekk = if (trekkvedtakId != null) trekkClient.hentSkattOgTrekk(pid, trekkvedtakId)?.andreTrekk else null
        val sorterteSatsperioder = andreTrekk?.satsperiodeListe?.sortedBy { it.fom } ?: emptyList()

        val lopendeSatsperioder = sorterteSatsperioder.filter { isLopende(it) }
        val fremtidigeSatsperioder = sorterteSatsperioder.filter { isFremtidig(it) }

        // Opphør løpende trekk, om det finnes
        if (trekkvedtakId != null && lopendeSatsperioder.isNotEmpty()) {
            opphorLoependeTrekk(pid, trekkvedtakId)
        }

        // Opphør fremtidige trekk om det finnes
        if (trekkvedtakId != null && fremtidigeSatsperioder.isNotEmpty()) {
            opphorFremtidigeTrekk(pid, trekkvedtakId)
        }
    }

    private fun skalOppretteNyttTrekk(tilleggstrekk: Double, andreTrekk: AndreTrekkResponse?): Boolean {
        if (andreTrekk == null && tilleggstrekk > 0) {
            return true
        } else if (andreTrekk != null && tilleggstrekk == 0.0) {
            return false
        }

        val finnesIkkeLopende = andreTrekk?.satsperiodeListe?.find { isLopende(it) } == null
        val finnesIkkeFremtidigTrekk = andreTrekk?.satsperiodeListe?.find { isFremtidig(it) } == null

        return finnesIkkeLopende || finnesIkkeFremtidigTrekk
    }

    private fun skalOppdatereTrekk(andreTrekk: AndreTrekkResponse?): Boolean = andreTrekk == null || andreTrekk.trekkvedtakId == null

    private fun opprettNyttTrekkRequest(pid: String, tilleggstrekk: Double, trekkalternativKode: String, brukersNavEnhet: String): AndreTrekkRequest =
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
            satsperiodeListe = listOf(opprettStatsperiode(tilleggstrekk)),
        )

    private fun lagOppdaterTrekkRequest(
        pid: String,
        satsType: SatsType,
        tilleggstrekk: Double,
        andreTrekk: AndreTrekkResponse
    ): AndreTrekkRequest {
        val satsperioder = (andreTrekk?.satsperiodeListe?.toMutableList() ?: mutableListOf()).plus(opprettStatsperiode(tilleggstrekk))
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

    private fun opprettStatsperiode(tilleggstrekk: Double): Satsperiode {
        val today = LocalDate.now()
        val forsteDatoNesteMaaned = today.plusMonths(1L).withDayOfMonth(1)
        val sisteDagDetteAret = LocalDate.of(today.year, 12, 31)
        return Satsperiode(
            fom = forsteDatoNesteMaaned,
            tom = sisteDagDetteAret,
            sats = tilleggstrekk.toBigDecimal()
        )
    }

    private fun opphorLoependeTrekk(pid: String, trekkvedtakId: Long) {
        val forsteDagNesteMaaned = LocalDate.now().plusMonths(1L).withDayOfMonth(1)
        val opphorAndreTrekkRequest = OpphorAndreTrekkRequest(kilde = Kilde.PPO1.name, trekkvedtakId, forsteDagNesteMaaned)
        trekkClient.opphorAndreTrekk(pid, opphorAndreTrekkRequest)
    }

    private fun opphorFremtidigeTrekk(pid: String, trekkvedtakId: Long) {
        val forsteDagNesteMaaned = LocalDate.now().plusMonths(1L).withDayOfMonth(1)
        val opphorAndreTrekkRequest = OpphorAndreTrekkRequest(kilde = Kilde.PPO1.name, trekkvedtakId, forsteDagNesteMaaned)
        trekkClient.opphorAndreTrekk(pid, opphorAndreTrekkRequest)
    }

    private fun isFremtidig(satsperiode: Satsperiode): Boolean {
        if (!satsperiode.erFeilregistrert!!) {
            val fom = satsperiode.fom
            val today = LocalDate.now()
            return today.isBefore(fom)
        }

        return false
    }

    private fun isLopende(satsperiode: Satsperiode): Boolean {
        if (satsperiode.erFeilregistrert == false) {
            val fom = satsperiode.fom
            val tom = satsperiode.tom
            val today = LocalDate.now()
            return today == fom || today == tom  || today.isAfter(fom) && today.isBefore(tom) // På randen
        }
        return false
    }

}