package no.nav.frivillig.skattetrekk.client.pdl.api

data class PdlQuery(
    val query: String,
    val variables: PdlPersonVariables,
)

data class PdlPersonVariables(
    val ident: String,
    val historisk: Boolean,
)
