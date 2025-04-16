package no.nav.frivillig.skattetrekk.client.pdl.api

import com.fasterxml.jackson.annotation.JsonProperty

data class PdlError(@JsonProperty("message") val message: String,
                    @JsonProperty("locations") val locations: List<PdlErrorLocation>,
                    @JsonProperty("path") val path: List<String>?,
                    @JsonProperty("extensions") val extensions: PdlErrorExtension?)

data class PdlErrorLocation(@JsonProperty("line") val line: Int,
                            @JsonProperty("column") val column: Int)

data class PdlErrorExtension(@JsonProperty("code") val code: PdlErrorCodes?,
                             @JsonProperty("classification") val classification: String?)