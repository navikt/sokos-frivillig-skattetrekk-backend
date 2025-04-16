package no.nav.frivillig.skattetrekk.endpoint.api

import no.nav.frivillig.skattetrekk.client.trekk.api.SatsType

data class TrekkDTO(
    val trekkvedtakId:Long?,
    val sats: Double?,
    val satsType: SatsType?,
)

data class Skattetrekk(
    val sats: Double?,
    val satsType: SatsType?
)

data class ForenkletSkattetrekk(
    val trekkvedtakId: Long?,
    val tabellNr: String?,
    val prosentsats: Int?
)

data class FrivilligSkattetrekkInitResponse(
    val tilleggstrekk: TrekkDTO?,
    val framtidigTilleggstrekk: TrekkDTO?,
    val skattetrekk: ForenkletSkattetrekk
)