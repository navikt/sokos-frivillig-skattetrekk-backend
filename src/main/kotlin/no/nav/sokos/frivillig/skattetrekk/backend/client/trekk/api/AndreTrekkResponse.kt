package no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api

import java.math.BigDecimal
import java.time.LocalDate

import com.fasterxml.jackson.annotation.JsonFormat

data class AndreTrekkResponse(
    val trekkvedtakId: Long?,
    val debitor: Bruker?,
    val trekktype: Trekktype?,
    val trekkstatus: Trekkstatus?,
    val kreditor: Bruker?,
    val kreditorAvdelingsnr: String?,
    val kreditorRef: String?,
    val kreditorKid: String?,
    val tssEksternId: String?,
    val prioritet: String?,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    val prioritetFom: LocalDate?,
    val trekkalternativ: Trekkalternativ?,
    val belopSaldotrekk: BigDecimal?,
    val belopTrukket: BigDecimal?,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    val datoOppfolging: LocalDate?,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    val gyldigTom: LocalDate?,
    val ansvarligEnhetId: String?,
    val sporing: Sporing?,
    val fagomradeListe: List<FagomradeResponse>?,
    val satsperiodeListe: List<Satsperiode>?,
)
