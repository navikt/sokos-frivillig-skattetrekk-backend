package no.nav.frivillig.skattetrekk.endpoint.api

import no.nav.frivillig.skattetrekk.client.trekk.api.SatsType
import java.time.LocalDate

data class FrivilligSkattetrekkInitResponse(
    val data: FrivilligSkattetrekkData?,
    val messages: List<FrivilligSkattetrekkMessage>?
)

data class FrivilligSkattetrekkData(
    val tilleggstrekk: TrekkDto?,
    val framtidigTilleggstrekk: FremtidigTrekkDto?,
    val skattetrekk: ForenkletSkattetrekkDto
)

data class TrekkDto(
    val sats: Double?,
    val satsType: SatsType?
)

data class FremtidigTrekkDto(
    val sats: Double?,
    val satsType: SatsType?,
    val gyldigFraOgMed: LocalDate? = null,
)

data class Skattetrekk(
    val sats: Double?,
    val satsType: SatsType?
)

data class ForenkletSkattetrekkDto(
    val tabellNr: String?,
    val prosentsats: Int?
)