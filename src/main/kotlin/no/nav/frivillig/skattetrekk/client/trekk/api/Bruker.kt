package no.nav.frivillig.skattetrekk.client.trekk.api

import com.fasterxml.jackson.annotation.JsonProperty

data class Bruker(
    @JsonProperty("id") val id: String?,
    @JsonProperty("navn") val navn: String?,
)
