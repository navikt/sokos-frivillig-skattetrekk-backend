package no.nav.frivillig.skattetrekk.client.trekk.api

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDate

data class Satsperiode(
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonProperty("fom") val fom: LocalDate?,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonProperty("tom") val tom: LocalDate?,
    @JsonProperty("sats") val sats: BigDecimal?,
    @JsonProperty("sporing") val sporing: Sporing? = null,
    @JsonProperty("erFeilregistrert") val erFeilregistrert: Boolean? = null,
)
