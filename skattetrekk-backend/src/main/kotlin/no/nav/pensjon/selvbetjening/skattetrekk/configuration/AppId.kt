package no.nav.pensjon.selvbetjening.skattetrekk.configuration

enum class AppId(
    val supportsTokenX: Boolean,
    val supportsFullmakt: Boolean
) {
    PENSJON_FULLMAKT(false, false),
    OPPDRAG_REST_PROXY(false, false),
    PDL(true, false),
    NORG2(false, false)
}