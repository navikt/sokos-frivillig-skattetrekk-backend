package no.nav.pensjon.selvbetjening.skattetrekk.client.trekk.api

import com.fasterxml.jackson.annotation.JsonProperty

data class Bruker(
    @JsonProperty("kreditorOffnr") val kreditorOffnr: String?,
    @JsonProperty("kreditorNavn") val kreditorNavn: String?,
)
