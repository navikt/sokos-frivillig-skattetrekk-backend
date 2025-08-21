package no.nav.frivillig.skattetrekk.endpoint.api

import no.nav.frivillig.skattetrekk.client.trekk.api.SatsType

data class BehandleRequest(
    val value: Int,
    val satsType: SatsType,
)
