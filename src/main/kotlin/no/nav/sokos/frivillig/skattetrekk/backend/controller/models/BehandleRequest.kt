package no.nav.sokos.frivillig.skattetrekk.backend.controller.models

import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.SatsType

data class BehandleRequest(
    val value: Int,
    val satsType: SatsType,
)
