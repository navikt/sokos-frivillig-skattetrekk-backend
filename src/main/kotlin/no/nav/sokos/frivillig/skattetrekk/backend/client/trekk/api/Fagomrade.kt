package no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api

import com.fasterxml.jackson.annotation.JsonProperty

data class Fagomrade(
    @JsonProperty("trekkgruppeKode") val trekkgruppeKode: String?,
    @JsonProperty("fagomradeKode") val fagomradeKode: String?,
    @JsonProperty("erFeilregistrert") val erFeilregistrert: Boolean?,
)
