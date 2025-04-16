package no.nav.frivillig.skattetrekk.client.trekk.api

import com.fasterxml.jackson.annotation.JsonProperty

data class Sporing(
    @JsonProperty("opprettetInfo") val opprettetInfo: Sporingsdetalj?,
)
