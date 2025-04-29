package no.nav.frivillig.skattetrekk.endpoint.api

import no.nav.frivillig.skattetrekk.client.trekk.api.SatsType

data class FrivilligSkattetrekkRequest(
    val trekkVedtakId: Long?,
    val value: Int,
    val satsType: SatsType
)