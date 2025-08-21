package no.nav.frivillig.skattetrekk.security

import com.fasterxml.jackson.annotation.JsonProperty

data class SetPidFilterErrorResponse(
    @JsonProperty("timestamp") val timestamp: String,
    @JsonProperty("status") val status: Int,
    @JsonProperty("error") val error: String,
    @JsonProperty("message") val message: ErrorCode,
    @JsonProperty("path") val path: String,
)
