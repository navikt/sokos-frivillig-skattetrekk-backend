package no.nav.sokos.frivillig.skattetrekk.backend.controller.models

import java.time.LocalDate

import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.SatsType

data class FrivilligSkattetrekkInitResponse(
    val data: FrivilligSkattetrekkData?,
    val messages: List<FrivilligSkattetrekkMessage>?,
)

data class FrivilligSkattetrekkData(
    val tilleggstrekk: TrekkDto?,
    val fremtidigTilleggstrekk: FremtidigTrekkDto?,
    val skattetrekk: ForenkletSkattetrekkDto,
    val maxBelop: Int,
    val maxProsent: Int,
)

data class TrekkDto(
    val sats: Int?,
    val satsType: SatsType?,
)

data class FremtidigTrekkDto(
    val sats: Int?,
    val satsType: SatsType?,
    val gyldigFraOgMed: LocalDate? = null,
)

data class ForenkletSkattetrekkDto(
    val tabellNr: String?,
    val prosentsats: Int?,
)
