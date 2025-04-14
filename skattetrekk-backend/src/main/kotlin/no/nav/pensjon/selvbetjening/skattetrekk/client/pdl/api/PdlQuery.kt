package no.nav.pensjon.selvbetjening.skattetrekk.client.pdl.api

data class PdlQuery(
    val query: String,
    val variables: PdlPersonVariables
)

data class PdlPersonVariables(
    val ident: String,
    val historisk: Boolean
)