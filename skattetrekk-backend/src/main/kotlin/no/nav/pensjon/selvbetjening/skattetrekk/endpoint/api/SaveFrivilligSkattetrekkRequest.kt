package no.nav.pensjon.selvbetjening.skattetrekk.endpoint.api

import no.nav.pensjon.selvbetjening.skattetrekk.client.trekk.api.SatsType


data class SaveFrivilligSkattetrekkRequest(
    val trekkVedtakId: Long?,
    val value: Int,
    val satsType: SatsType
)