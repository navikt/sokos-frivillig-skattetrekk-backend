package no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api

import com.fasterxml.jackson.annotation.JsonProperty

data class Trekkalternativ(
    @JsonProperty("kode")
    val kode: String?,
    @JsonProperty("dekode")
    val dekode: String?,
)
