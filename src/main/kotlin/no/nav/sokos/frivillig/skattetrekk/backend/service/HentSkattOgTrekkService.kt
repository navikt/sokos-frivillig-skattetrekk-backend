package no.nav.sokos.frivillig.skattetrekk.backend.service

import java.time.LocalDate

import mu.KotlinLogging
import org.springframework.stereotype.Service

import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.TrekkClient
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.AndreTrekkResponse
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.SatsType
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.Satsperiode
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.Skattetrekk
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.TrekkInfo
import no.nav.sokos.frivillig.skattetrekk.backend.controller.OppdragUtilgjengeligException
import no.nav.sokos.frivillig.skattetrekk.backend.controller.models.ForenkletSkattetrekkDto
import no.nav.sokos.frivillig.skattetrekk.backend.controller.models.FremtidigTrekkDto
import no.nav.sokos.frivillig.skattetrekk.backend.controller.models.FrivilligSkattetrekkData
import no.nav.sokos.frivillig.skattetrekk.backend.controller.models.FrivilligSkattetrekkInitResponse
import no.nav.sokos.frivillig.skattetrekk.backend.controller.models.FrivilligSkattetrekkMessage
import no.nav.sokos.frivillig.skattetrekk.backend.controller.models.FrivilligSkattetrekkMessageCode
import no.nav.sokos.frivillig.skattetrekk.backend.controller.models.FrivilligSkattetrekkType
import no.nav.sokos.frivillig.skattetrekk.backend.controller.models.TrekkDto
import no.nav.sokos.frivillig.skattetrekk.backend.util.isDateInPeriod

private val logger = KotlinLogging.logger {}
private const val TREKK_KODE_LOPP: String = "LOPP" // Prosenttrekk

@Service
class HentSkattOgTrekkService(
    private val trekkClient: TrekkClient,
) {
    fun hentSkattetrekk(pid: String): FrivilligSkattetrekkInitResponse? {
        try {
            logger.info("Finner trekkliste for forskuddsskatt og frivillig skattetrekk")
            val forskuddsTrekkListe = trekkClient.finnTrekkListe(pid, TrekkTypeCode.FSKT)
            val tilleggsTrekkInfoListe = trekkClient.finnTrekkListe(pid, TrekkTypeCode.FRIS)

            logger.info("Henter skatt og trekk for forskuddsskatt og frivillig skattetrekk")
            val skattetrekk = finnSkattetrekk(pid, forskuddsTrekkListe)
            val tillegstrekkVedtakListe = finnTilleggstrekkListe(pid, tilleggsTrekkInfoListe)

            return createSkattetrekkInitResponse(skattetrekk, tillegstrekkVedtakListe, mutableListOf())
        } catch (e: OppdragUtilgjengeligException) {
            return createSkattetrekkInitResponse(
                null,
                emptyList(),
                mutableListOf(
                    FrivilligSkattetrekkMessage(
                        code = FrivilligSkattetrekkMessageCode.OPPDRAG_UTILGJENGELIG,
                        type = FrivilligSkattetrekkType.INFO,
                    ),
                ),
            )
        }
    }

    private fun createSkattetrekkInitResponse(
        skattetrekk: Skattetrekk?,
        tilleggstrekkListe: List<AndreTrekkResponse?>,
        meldinger: MutableList<FrivilligSkattetrekkMessage>,
    ): FrivilligSkattetrekkInitResponse {
        val forenkletSkattetrekk = determineForenkletSkattetrekk(skattetrekk)
        val currentTilleggstrekk = tilleggstrekkListe.find { it?.satsperiodeListe?.toList()?.findRunningSatsperiode() == true }
        val fremtidigeTrekk = tilleggstrekkListe.filter { it?.satsperiodeListe?.toList()?.hasNextSatsperiode() == true }

        val currentTrekkDto = toThisTrekkDto(currentTilleggstrekk)
        val nextTilleggstrekk = findNextTilleggstrekk(currentTilleggstrekk, fremtidigeTrekk)

        if (nextTilleggstrekk?.sats == 0) {
            meldinger.add(
                FrivilligSkattetrekkMessage(
                    code = FrivilligSkattetrekkMessageCode.OPPHØR_REGISTRERT,
                    type = FrivilligSkattetrekkType.INFO,
                ),
            )
        }

        return FrivilligSkattetrekkInitResponse(
            messages = meldinger,
            data =
                FrivilligSkattetrekkData(
                    tilleggstrekk = currentTrekkDto,
                    fremtidigTilleggstrekk = nextTilleggstrekk,
                    skattetrekk = forenkletSkattetrekk,
                    maxBelop = Validering.MAX_BELOP,
                    maxProsent = Validering.MAX_PROSENT,
                ),
        )
    }

    private fun toThisTrekkDto(currentTilleggstrekk: AndreTrekkResponse?): TrekkDto? {
        val currentSatsperiode =
            currentTilleggstrekk?.satsperiodeListe?.firstOrNull { isDateInPeriod(LocalDate.now(), it.fom, it.tom) }
        return if (currentTilleggstrekk != null && currentSatsperiode != null) {
            TrekkDto(
                sats = currentSatsperiode.sats?.toInt(),
                satsType =
                    currentTilleggstrekk.trekkalternativ?.kode?.let {
                        if (it ==
                            TREKK_KODE_LOPP
                        ) {
                            SatsType.PROSENT
                        } else {
                            SatsType.KRONER
                        }
                    },
            )
        } else {
            null
        }
    }

    private fun findNextTilleggstrekk(
        currentTilleggstrekk: AndreTrekkResponse?,
        fremtidigeTrekk: List<AndreTrekkResponse?>,
    ): FremtidigTrekkDto? {
        val currentSatsperiodeTom =
            currentTilleggstrekk?.satsperiodeListe?.firstOrNull { isDateInPeriod(LocalDate.now(), it.fom, it.tom) }?.tom
        val ingenFremtidigeTrekk = fremtidigeTrekk.isEmpty()
        val nesteMaaned = LocalDate.now().plusMonths(1L).withDayOfMonth(1)
        if (
            currentSatsperiodeTom != null &&
            // Sjekk at det finnes en gyldig satsperiode
            (LocalDate.now() == currentSatsperiodeTom || LocalDate.now().isBefore(currentSatsperiodeTom)) &&
            nesteMaaned.isAfter(currentSatsperiodeTom) &&
            ingenFremtidigeTrekk
        ) {
            return FremtidigTrekkDto(
                sats = 0,
                satsType = null,
                gyldigFraOgMed = currentSatsperiodeTom.plusMonths(1L)?.withDayOfMonth(1), // Neste månedens første dag,
            )
        }

        return fremtidigeTrekk.find { it?.satsperiodeListe?.toList()?.hasNextSatsperiode() == true }?.mapToFremtidigTrekk(nesteMaaned)
    }

    private fun AndreTrekkResponse.mapToFremtidigTrekk(nesteMaaned: LocalDate): FremtidigTrekkDto =
        FremtidigTrekkDto(
            sats =
                this.satsperiodeListe
                    ?.find { isStartingFirstOfNextMonth(it) }
                    ?.sats
                    ?.toInt(),
            satsType = this.trekkalternativ?.kode?.let { if (it == TREKK_KODE_LOPP) SatsType.PROSENT else SatsType.KRONER },
            gyldigFraOgMed = nesteMaaned,
        )

    private fun determineForenkletSkattetrekk(skattetrekk: Skattetrekk?): ForenkletSkattetrekkDto {
        val tabellNr = skattetrekk?.tabellnr?.trim()
        val prosentsats = skattetrekk?.prosentsats

        if (!tabellNr.isNullOrEmpty() && "0000" != tabellNr) return ForenkletSkattetrekkDto(tabellNr, null)
        if (prosentsats != null) return ForenkletSkattetrekkDto(null, prosentsats)

        return ForenkletSkattetrekkDto(null, null)
    }

    private fun List<Satsperiode>.findRunningSatsperiode() = this.find { isDateInPeriod(LocalDate.now(), it.fom, it.tom) } != null

    private fun List<Satsperiode>.hasNextSatsperiode() = this.find { isStartingFirstOfNextMonth(it) } != null

    private fun isStartingFirstOfNextMonth(satsperiode: Satsperiode): Boolean =
        if (satsperiode.erFeilregistrert == null ||
            !satsperiode.erFeilregistrert
        ) {
            val fom = satsperiode.fom
            val firstOfNextMonth = LocalDate.now().plusMonths(1L).withDayOfMonth(1)
            fom == firstOfNextMonth
        } else {
            false
        }

    private fun finnSkattetrekk(
        pid: String,
        trekkInfoListe: List<TrekkInfo>?,
    ): Skattetrekk? {
        val skattetrekk: Skattetrekk? = null

        if (trekkInfoListe != null) {
            val forskuddskatt = if (trekkInfoListe.isNotEmpty()) trekkInfoListe[0] else null
            if (forskuddskatt?.trekkvedtakId != null) {
                return trekkClient.hentSkattOgTrekk(pid, forskuddskatt.trekkvedtakId)?.skattetrekk
            }
        }
        return skattetrekk
    }

    private fun finnTilleggstrekkListe(
        pid: String,
        tilleggsTrekkInfoListe: List<TrekkInfo>?,
    ) = tilleggsTrekkInfoListe
        ?.map {
            if (it.trekkvedtakId != null) trekkClient.hentSkattOgTrekk(pid, it.trekkvedtakId)?.andreTrekk else null
        } ?: emptyList()
}
