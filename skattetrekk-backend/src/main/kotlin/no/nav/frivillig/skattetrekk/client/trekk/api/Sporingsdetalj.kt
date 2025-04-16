package no.nav.frivillig.skattetrekk.client.trekk.api

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class Sporingsdetalj(
    @JsonProperty("kilde") val kilde: String?,
    @JsonProperty("opprettetAvId") val opprettetAvId: String?,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonProperty("opprettetDato") val opprettetDato: LocalDate?,
)
