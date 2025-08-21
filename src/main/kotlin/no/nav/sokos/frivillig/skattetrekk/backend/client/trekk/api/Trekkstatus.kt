package no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api

import com.fasterxml.jackson.annotation.JsonProperty

data class Trekkstatus(
    @JsonProperty("kode") val kode: String?,
    @JsonProperty("dekode") val dekode: String?,
)
