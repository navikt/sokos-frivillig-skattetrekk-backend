package no.nav.frivillig.skattetrekk.service

import no.nav.frivillig.skattetrekk.client.trekk.TrekkClient
import no.nav.frivillig.skattetrekk.client.trekk.api.AndreTrekkRequest
import no.nav.frivillig.skattetrekk.client.trekk.api.Fagomrade
import no.nav.frivillig.skattetrekk.client.trekk.api.SatsType
import no.nav.frivillig.skattetrekk.client.trekk.api.Satsperiode
import no.nav.frivillig.skattetrekk.security.SecurityContextUtil
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

    fun behandleTrekk(trekkvedtakId: Long?, verdi: Int, satsType: SatsType) {
        log.info("Bruker har valgt trekk i $satsType")

        val pid = SecurityContextUtil.getPidFromContext()
        val hentSkattOgTrekkResponse = if (trekkvedtakId != null) trekkClient.hentSkattOgTrekk(pid, trekkvedtakId) else null

        val sorterteSatsperioder = hentSkattOgTrekkResponse?.andreTrekk?.satsperiodeListe?.sortedBy { it.fom } ?: emptyList()
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
        if (tilleggstrekk > 0) {
            val trekkalternativKode = if( satsType == SatsType.KRONER) TrekkalternativKode.LOPM else TrekkalternativKode.LOPP
            val brukersNavEnhet = geografiskLokasjonService.hentNavEnhet(pid)

            trekkClient.opprettAndreTrekk(
                pid,
                OpprettAndreTrekkRequest(
                    Kilde.PPO1.name,
                    opprettNyttTrekk(pid, tilleggstrekk, trekkalternativKode.name, brukersNavEnhet)
                ))
        }
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

    private fun opprettNyttTrekk(pid: String, tilleggstrekk: Double, trekkalternativKode: String, brukersNavEnhet: String): AndreTrekkRequest =
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
}