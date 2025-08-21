package no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api

import com.fasterxml.jackson.annotation.JsonProperty

data class OpprettAndreTrekkRequest(
    @JsonProperty("kilde") val kilde: String,
    @JsonProperty("andreTrekk") val andreTrekk: AndreTrekkRequest,
)
