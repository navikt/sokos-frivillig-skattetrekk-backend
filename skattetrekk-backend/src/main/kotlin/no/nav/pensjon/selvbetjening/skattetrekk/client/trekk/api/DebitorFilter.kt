package no.nav.pensjon.selvbetjening.skattetrekk.client.trekk.api

import com.fasterxml.jackson.annotation.JsonProperty

data class DebitorFilter(
    @JsonProperty("tssEksternId") val tssEksternId: String? = null,
    @JsonProperty("trekkgruppeKode") val trekkgruppeKode: String? = null,
    @JsonProperty("trekktypeKode") val trekktypeKode: String? = null,
    @JsonProperty("fagomradeKode") val fagomradeKode: String? = null,
    @JsonProperty("visAvsluttede") val visAvsluttede: Boolean? = null,
)
