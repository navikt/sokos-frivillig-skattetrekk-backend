package no.nav.frivillig.skattetrekk.client.trekk.api

data class OppdaterAndreTrekkRequest(
    val trekkvedtakId: Long,
    val andreTrekk: AndreTrekkRequest,
    val kilde: String,
)
