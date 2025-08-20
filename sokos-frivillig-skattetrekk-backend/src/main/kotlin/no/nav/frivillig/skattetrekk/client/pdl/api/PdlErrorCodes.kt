package no.nav.frivillig.skattetrekk.client.pdl.api

import com.fasterxml.jackson.annotation.JsonProperty

enum class PdlErrorCodes {
    @JsonProperty("unauthenticated")
    UNAUTHENTICATED,

    @JsonProperty("unauthorized")
    UNAUTHORIZED,

    @JsonProperty("not_found")
    NOT_FOUND,

    @JsonProperty("bad_request")
    BAD_REQUEST,

    @JsonProperty("server_error")
    SERVER_ERROR,
}
