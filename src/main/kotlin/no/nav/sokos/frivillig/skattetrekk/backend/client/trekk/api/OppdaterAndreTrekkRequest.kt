package no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api

data class OppdaterAndreTrekkRequest(
    val trekkvedtakId: Long,
    val andreTrekk: AndreTrekkRequest,
    val kilde: String,
)
