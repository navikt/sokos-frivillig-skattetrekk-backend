package no.nav.frivillig.skattetrekk.client.trekk.api

import com.fasterxml.jackson.annotation.JsonProperty

data class OpprettAndreTrekkResponse(
    @JsonProperty("trekkvedtakId") val trekkvedtakId: Long?,
)