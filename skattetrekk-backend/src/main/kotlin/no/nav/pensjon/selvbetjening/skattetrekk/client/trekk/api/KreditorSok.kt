package no.nav.pensjon.selvbetjening.skattetrekk.client.trekk.api

data class KreditorSok(
    val tssEksternId: String?,
    val kreditorOffnr: String?,
    val kreditorAvdelingsnr: String?,
    val filter: KreditorFilter,
)
