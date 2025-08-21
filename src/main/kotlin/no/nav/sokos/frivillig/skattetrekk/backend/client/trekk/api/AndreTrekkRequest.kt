package no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api

import java.math.BigDecimal
import java.time.LocalDate

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

data class AndreTrekkRequest(
    @JsonProperty("ansvarligEnhetId") val ansvarligEnhetId: String,
    @JsonProperty("belopSaldotrekk") val belopSaldotrekk: BigDecimal? = null,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonProperty("datoOppfolging") val datoOppfolging: LocalDate? = null,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonProperty("gyldigTom") val gyldigTom: LocalDate? = null,
    @JsonProperty("debitorOffnr") val debitorOffnr: String,
    @JsonProperty("trekkalternativKode") val trekkalternativKode: String,
    @JsonProperty("trekktypeKode") val trekktypeKode: String,
    @JsonProperty("tssEksternId") val tssEksternId: String? = null,
    @JsonProperty("kreditorKid") val kreditorKid: String? = null,
    @JsonProperty("kreditorRef") val kreditorRef: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonProperty("prioritetFom") val prioritetFom: LocalDate? = null,
    @JsonProperty("satsperiodeListe") val satsperiodeListe: List<Satsperiode>,
    @JsonProperty("fagomradeListe") val fagomradeListe: List<Fagomrade>?,
)
