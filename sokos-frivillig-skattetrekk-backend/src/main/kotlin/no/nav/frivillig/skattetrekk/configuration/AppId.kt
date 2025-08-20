package no.nav.frivillig.skattetrekk.configuration

enum class AppId(
    val supportsTokenX: Boolean,
) {
    OPPDRAG_REST_PROXY(false),
    PDL(true),
    NORG2(false),
}
