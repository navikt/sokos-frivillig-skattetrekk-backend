package no.nav.pensjon.selvbetjening.skattetrekk.client.trekk.api

data class DebitorSok(
    val debitorOffnr: String?,
    val filter: DebitorFilter,
)
