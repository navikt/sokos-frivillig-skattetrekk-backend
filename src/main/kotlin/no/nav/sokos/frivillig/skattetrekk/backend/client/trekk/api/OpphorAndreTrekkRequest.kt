package no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api

import java.time.LocalDate

import com.fasterxml.jackson.annotation.JsonFormat

data class OpphorAndreTrekkRequest(
    val kilde: String,
    val trekkvedtakId: Long,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    val opphorFom: LocalDate,
)
