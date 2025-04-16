package no.nav.frivillig.skattetrekk.client.norg2.api

import com.fasterxml.jackson.annotation.JsonProperty

data class NavEnhetResponse(
    @JsonProperty("enhetId") val enhetId: Long
)