package no.nav.frivillig.skattetrekk.endpoint.api

import no.nav.pensjon.selvbetjening.skattetrekk.client.trekk.api.SatsType


data class SaveFrivilligSkattetrekkRequest(
    val trekkVedtakId: Long?,
    val value: Int,
    val satsType: SatsType
)