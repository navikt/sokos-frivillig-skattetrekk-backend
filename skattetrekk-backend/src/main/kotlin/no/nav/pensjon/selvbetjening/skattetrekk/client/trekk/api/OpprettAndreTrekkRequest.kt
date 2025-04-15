package no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.pensjon.selvbetjening.skattetrekk.client.trekk.api.AndreTrekkRequest

data class OpprettAndreTrekkRequest(
    @JsonProperty("kilde") val kilde: String,
    @JsonProperty("andreTrekk") val andreTrekk: AndreTrekkRequest,
)


