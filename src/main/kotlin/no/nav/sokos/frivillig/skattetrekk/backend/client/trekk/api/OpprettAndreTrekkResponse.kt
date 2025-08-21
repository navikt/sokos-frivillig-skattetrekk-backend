package no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api

import com.fasterxml.jackson.annotation.JsonProperty

data class OpprettAndreTrekkResponse(
    @JsonProperty("trekkvedtakId") val trekkvedtakId: Long?,
)
