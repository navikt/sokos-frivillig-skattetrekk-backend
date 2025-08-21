package no.nav.frivillig.skattetrekk.client.trekk.api

import com.fasterxml.jackson.annotation.JsonProperty

data class OpprettAndreTrekkRequest(
    @JsonProperty("kilde") val kilde: String,
    @JsonProperty("andreTrekk") val andreTrekk: AndreTrekkRequest,
)
