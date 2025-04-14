package no.nav.pensjon.selvbetjening.skattetrekk.client.trekk.api

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDate

data class TrekkInfo(
    @JsonProperty("trekkvedtakId") val trekkvedtakId: Long?,
    @JsonProperty("debitor") val debitor: Bruker?,
    @JsonProperty("trekktype") val trekktype: Trekktype?,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonProperty("trekkperiodeFom") val trekkperiodeFom: LocalDate?,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonProperty("trekkperiodeTom") val trekkperiodeTom: LocalDate?,
    @JsonProperty("trekkstatus") val trekkstatus: Trekkstatus?,
    @JsonProperty("kreditor") val kreditor: Bruker?,
    @JsonProperty("kreditorRef") val kreditorRef: String?,
    @JsonProperty("tssEksternId") val tssEksternId: String?,
    @JsonProperty("trekkalternativ") val trekkalternativ: Trekkalternativ?,
    @JsonProperty("sats") val sats: BigDecimal?,
    @JsonProperty("belopSaldotrekk") val belopSaldotrekk: BigDecimal?,
    @JsonProperty("belopTrukket") val belopTrukket: BigDecimal?,
    @JsonProperty("ansvarligEnhetId") val ansvarligEnhetId: String?,
)
