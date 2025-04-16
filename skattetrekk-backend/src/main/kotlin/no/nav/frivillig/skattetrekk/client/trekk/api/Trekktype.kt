package no.nav.frivillig.skattetrekk.client.trekk.api

import com.fasterxml.jackson.annotation.JsonProperty

data class Trekktype(
    @JsonProperty("kode") val kode: String?,
    @JsonProperty("dekode") val dekode: String?,
)
