package no.nav.pensjon.selvbetjening.skattetrekk.client.trekk.api

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.LocalDate

data class Satsperiode(
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    val fom: LocalDate?,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    val tom: LocalDate?,
    val sats: BigDecimal?,
    val sporing: Sporing? = null,
    val erFeilregistrert: Boolean? = null,
)
