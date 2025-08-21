package no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api

import java.time.LocalDate

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

data class Skattetrekk(
    @JsonProperty("trekkvedtakId") val trekkvedtakId: Long?,
    @JsonProperty("debitor") val debitor: Bruker?,
    @JsonProperty("trekktype") val trekktype: Trekktype?,
    @JsonProperty("trekkstatus") val trekkstatus: Trekkstatus?,
    @JsonProperty("skattekommunenr") val skattekommunenr: String?,
    @JsonProperty("skattekommuneNavn") val skattekommuneNavn: String?,
    @JsonProperty("tabellnr") val tabellnr: String?,
    @JsonProperty("prosentsats") val prosentsats: Int?,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonProperty("frikortFom") val frikortFom: LocalDate?,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonProperty("frikortTom") val frikortTom: LocalDate?,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonProperty("trekkperiodeFom") val trekkperiodeFom: LocalDate?,
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Oslo")
    @JsonProperty("trekkperiodeTom") val trekkperiodeTom: LocalDate?,
    @JsonProperty("tabellIFaggruppe") val tabellIFaggruppe: Faggruppe?,
    @JsonProperty("sporing") val sporing: Sporing?,
)
