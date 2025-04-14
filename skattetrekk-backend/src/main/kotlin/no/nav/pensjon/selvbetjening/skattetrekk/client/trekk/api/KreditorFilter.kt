package no.nav.pensjon.selvbetjening.skattetrekk.client.trekk.api

data class KreditorFilter(
    val trekkgruppeKode: String?,
    val trekktypeKode: String?,
    val fagomradeKode: String?,
    val visAvsluttede: Boolean,
)
