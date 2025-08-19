package no.nav.frivillig.skattetrekk.security

data class AuthenticatedUserDetails(
    val pid: String,
    val isFullmakt: Boolean
)