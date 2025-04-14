package no.nav.pensjon.selvbetjening.skattetrekk.client.trekk.api

import com.fasterxml.jackson.annotation.JsonProperty

data class Sporing(
    @JsonProperty("opprettetInfo") val opprettetInfo: Sporingsdetalj?,
)
