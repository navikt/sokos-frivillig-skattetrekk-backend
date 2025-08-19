package no.nav.frivillig.skattetrekk.client.trekk.api

import com.fasterxml.jackson.annotation.JsonProperty

data class FinnTrekkListeResponse(
    @JsonProperty("trekkInfoListe") val trekkInfoListe: List<TrekkInfo>,
)

