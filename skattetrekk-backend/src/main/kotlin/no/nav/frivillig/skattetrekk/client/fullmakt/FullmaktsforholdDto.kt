package no.nav.frivillig.skattetrekk.client.fullmakt

import com.fasterxml.jackson.annotation.JsonProperty

data class FullmaktsforholdDto (
    @JsonProperty("harFullmaktsforhold") val harFullmaktsforhold: Boolean?,
    @JsonProperty("erPersonligFullmakt") val erPersonligFullmakt: Boolean?
)
