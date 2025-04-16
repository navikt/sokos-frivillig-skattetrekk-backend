package no.nav.oppdrag_rest_proxy.oppdragssystemet.skattogtrekk.trekk

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class HarBsktTrekkRequest(
    val fnr: String,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    val dato: LocalDate? = null,
)
