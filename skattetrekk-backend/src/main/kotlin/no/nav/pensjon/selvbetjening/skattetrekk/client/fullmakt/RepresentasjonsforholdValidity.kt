package no.nav.pensjon.selvbetjening.skattetrekk.client.fullmakt

import com.fasterxml.jackson.annotation.JsonProperty

data class RepresentasjonsforholdValidity (
    @JsonProperty("hasValidRepresentasjonsforhold") val hasValidRepresentasjonsforhold: Boolean,
    @JsonProperty("fullmaktsgiverNavn") val fullmaktsgiverNavn: String?,
    @JsonProperty("fullmaktsgiverFnrKryptert") val fullmaktsgiverFnrKryptert: String,
    @JsonProperty("fullmaktsgiverFnr") val fullmaktsgiverFnr: String
)
