package no.nav.pensjon.selvbetjening.skattetrekk.client.trekk.api

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.LocalDate

data class AndreTrekkRequest(
    val ansvarligEnhetId: String,
    val belopSaldotrekk: BigDecimal? = null,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    val datoOppfolging: LocalDate? = null,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    val gyldigTom: LocalDate? = null,
    val debitorOffnr: String,
    val trekkalternativKode: String,
    val trekktypeKode: String,
    val tssEksternId: String? = null,
    val kreditorKid: String? = null,
    val kreditorRef: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    val prioritetFom: LocalDate? = null,
    val satsperiodeListe: List<Satsperiode>,
    val fagomradeListe: List<Fagomrade>,
)
