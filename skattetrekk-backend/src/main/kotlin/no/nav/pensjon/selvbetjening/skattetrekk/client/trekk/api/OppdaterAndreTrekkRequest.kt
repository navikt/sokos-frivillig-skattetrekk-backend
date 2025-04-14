package no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy

import no.nav.pensjon.selvbetjening.skattetrekk.client.trekk.api.AndreTrekkRequest

data class OppdaterAndreTrekkRequest(
    val trekkvedtakId: Long,
    val andreTrekk: AndreTrekkRequest,
    val kilde: String,
)
