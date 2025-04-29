package no.nav.frivillig.skattetrekk.endpoint.api

import no.nav.frivillig.skattetrekk.client.trekk.api.SatsType

data class OpprettFrivilligSkattetrekkRequest(
    val value: Int,
    val satsType: SatsType
)