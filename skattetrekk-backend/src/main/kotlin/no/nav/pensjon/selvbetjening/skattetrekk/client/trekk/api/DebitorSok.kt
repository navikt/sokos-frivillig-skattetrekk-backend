package no.nav.pensjon.selvbetjening.skattetrekk.client.trekk.api

import com.fasterxml.jackson.annotation.JsonProperty

data class DebitorSok(
    @JsonProperty("debitorOffnr") val debitorOffnr: String?,
    @JsonProperty("filter") val filter: DebitorFilter,
)
