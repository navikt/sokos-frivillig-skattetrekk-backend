package no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api

import java.time.LocalDate

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

data class Sporingsdetalj(
    @JsonProperty("kilde")
    val kilde: String?,
    @JsonProperty("opprettetAvId")
    val opprettetAvId: String?,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonProperty("opprettetDato")
    val opprettetDato: LocalDate?,
)
