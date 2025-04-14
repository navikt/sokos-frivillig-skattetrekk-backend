package no.nav.pensjon.selvbetjening.skattetrekk.security

data class AuthenticatedUserDetails(
    val pid: String,
    val isFullmakt: Boolean
)