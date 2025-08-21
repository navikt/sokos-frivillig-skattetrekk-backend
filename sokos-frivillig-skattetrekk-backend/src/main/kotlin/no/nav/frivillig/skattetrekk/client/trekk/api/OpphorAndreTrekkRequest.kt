package no.nav.frivillig.skattetrekk.client.trekk.api

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class OpphorAndreTrekkRequest(
    val kilde: String,
    val trekkvedtakId: Long,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    val opphorFom: LocalDate,
)
