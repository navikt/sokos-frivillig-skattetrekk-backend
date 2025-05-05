package no.nav.frivillig.skattetrekk.service

import no.nav.frivillig.skattetrekk.client.trekk.TrekkClient
import no.nav.frivillig.skattetrekk.client.trekk.api.*
import no.nav.frivillig.skattetrekk.endpoint.api.ForenkletSkattetrekkDto
import no.nav.frivillig.skattetrekk.endpoint.api.FrivilligSkattetrekkInitResponse
import no.nav.frivillig.skattetrekk.endpoint.api.TrekkDto
import no.nav.frivillig.skattetrekk.util.isDateInPeriod
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

@Service
class HentSkattOgTrekkService(
    private val trekkClient: TrekkClient
) {

    private val log = LoggerFactory.getLogger(HentSkattOgTrekkService::class.java)

    val TREKK_KODE_LOPP: String = "LOPP" // Prosenttrekk
    val IsoDateFormatter = SimpleDateFormat("yyyy-MM-dd")

    fun hentSkattetrekk(pid: String): FrivilligSkattetrekkInitResponse? {

        var forskuddsTrekkListe: List<TrekkInfo>? = null
        var tilleggsTrekkInfoListe: List<TrekkInfo>? = null

        try {
            forskuddsTrekkListe = trekkClient.finnTrekkListe(pid, TrekkTypeCode.FSKT)
            tilleggsTrekkInfoListe = trekkClient.finnTrekkListe(pid, TrekkTypeCode.FRIS)
        } catch (e: Exception) {
            log.error(e.message)
        }

        val skattetrekk = finnSkattetrekk(pid, forskuddsTrekkListe)
        val tillegstrekkVedtakListe = opprettTilleggstrekkVedtakListe(pid, tilleggsTrekkInfoListe)

        return createSkattetrekkInitResponse(skattetrekk, tillegstrekkVedtakListe)
    }

    private fun createSkattetrekkInitResponse(skattetrekk: Skattetrekk?, tilleggstrekkListe: List<AndreTrekkResponse?>): FrivilligSkattetrekkInitResponse {

        val forenkletSkattetrekk = determineForenkletSkattetrekk(skattetrekk)
        val currentTilleggstrekk = tilleggstrekkListe.find { it?.satsperiodeListe?.toList()?.findRunningSatsperiode() == true }

        val nextTilleggstrekk = tilleggstrekkListe.find { it?.satsperiodeListe?.toList()?.hasNextSatsperiode() == true }

        return FrivilligSkattetrekkInitResponse(
            tilleggstrekk = currentTilleggstrekk?.mapToTrekkDTO(),
            framtidigTilleggstrekk = nextTilleggstrekk?.mapToTrekkDTO(),
            skattetrekk = forenkletSkattetrekk
        )
    }

    private fun AndreTrekkResponse.mapToTrekkDTO(): TrekkDto = TrekkDto(
        trekkvedtakId = this.trekkvedtakId,
        sats = this.satsperiodeListe?.first()?.sats?.toDouble(),
        satsType = this.trekkalternativ?.kode?.let { if (it == TREKK_KODE_LOPP) SatsType.PROSENT else SatsType.KRONER }
    )

    private fun determineForenkletSkattetrekk(skattetrekk: Skattetrekk?): ForenkletSkattetrekkDto {

        val trekkVedtakId = skattetrekk?.trekkvedtakId
        val tabellNr = skattetrekk?.tabellnr?.trim()
        val prosentsats = skattetrekk?.prosentsats

        if (!tabellNr.isNullOrEmpty() && "0000" != tabellNr) return ForenkletSkattetrekkDto(trekkVedtakId, tabellNr, null)
        if (prosentsats != null) return ForenkletSkattetrekkDto(trekkVedtakId, null, prosentsats)

        //TODO: Bedre expection handering i systemet
        //throw Exception("No valid skattetrekk found")
        return ForenkletSkattetrekkDto(trekkVedtakId, null, null)
    }

    private fun isThisSatsperiodeCreatedAfterNext(satsperiode: Satsperiode, nextSatsperiode: Satsperiode?): Boolean {
        val nextSatsperiodeDate = IsoDateFormatter.parse(nextSatsperiode?.sporing?.opprettetInfo?.opprettetDato!!.toString())
        val satsperiodeDate = IsoDateFormatter.parse(satsperiode.sporing?.opprettetInfo?.opprettetDato.toString())
        return nextSatsperiodeDate.before(satsperiodeDate)
    }

    private fun List<Satsperiode>.findRunningSatsperiode() = this.find { isDateInPeriod(Date(), IsoDateFormatter.parse(it.fom.toString()), IsoDateFormatter.parse(it.tom.toString())) } != null
    private fun List<Satsperiode>.hasNextSatsperiode() = this.find { isStartingFirstOfNextMonth(it) } != null

    private fun isStartingFirstOfNextMonth(satsperiode: Satsperiode): Boolean = if (satsperiode.erFeilregistrert == null || !satsperiode.erFeilregistrert) {
        val fom = satsperiode.fom
        val firstOfNextMonth = LocalDate.now().plusMonths(1L).withDayOfMonth(1)
        fom == firstOfNextMonth
    } else false

    private fun finnSkattetrekk(pid: String, trekkInfoListe: List<TrekkInfo>?): Skattetrekk? {
        val skattetrekk: Skattetrekk? = null

        if (trekkInfoListe != null) {
            val forskuddskatt = if (trekkInfoListe.isNotEmpty()) trekkInfoListe[0] else null
            if (forskuddskatt?.trekkvedtakId != null) {
                return trekkClient.hentSkattOgTrekk(pid, forskuddskatt.trekkvedtakId).skattetrekk
            }
        }
        return skattetrekk
    }

    private fun opprettTilleggstrekkVedtakListe(pid: String, tilleggsTrekkInfoListe: List<TrekkInfo>?) = tilleggsTrekkInfoListe
        ?.map {
            if (it.trekkvedtakId != null) trekkClient.hentSkattOgTrekk(pid, it.trekkvedtakId).andreTrekk else null
        } ?: emptyList()
}