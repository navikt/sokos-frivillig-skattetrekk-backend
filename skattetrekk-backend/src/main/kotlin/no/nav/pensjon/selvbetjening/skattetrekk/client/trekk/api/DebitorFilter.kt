package no.nav.pensjon.selvbetjening.skattetrekk.client.trekk.api

data class DebitorFilter(
    val tssEksternId: String? = null,
    val trekkgruppeKode: String? = null,
    val trekktypeKode: String? = null,
    val fagomradeKode: String? = null,
    val visAvsluttede: Boolean? = null,
)
