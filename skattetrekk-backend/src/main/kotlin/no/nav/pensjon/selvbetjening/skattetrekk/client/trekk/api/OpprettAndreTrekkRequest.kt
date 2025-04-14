package no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy

import no.nav.pensjon.selvbetjening.skattetrekk.client.trekk.api.AndreTrekkRequest

data class OpprettAndreTrekkRequest(
    val kilde: String,
    val andreTrekk: AndreTrekkRequest,
)


