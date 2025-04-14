package no.nav.pensjon.selvbetjening.skattetrekk.client.fullmakt

import com.fasterxml.jackson.annotation.JsonProperty

data class FullmaktsforholdDto (
    @JsonProperty("harFullmaktsforhold") val harFullmaktsforhold: Boolean?,
    @JsonProperty("erPersonligFullmakt") val erPersonligFullmakt: Boolean?
)
