package no.nav.pensjon.selvbetjening.skattetrekk.client.trekk.api

import com.fasterxml.jackson.annotation.JsonProperty

data class FinnTrekkListeResponse(
    @JsonProperty("trekkInfoListe") val trekkInfoListe: List<TrekkInfo>,
)

