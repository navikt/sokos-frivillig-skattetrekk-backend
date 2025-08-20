package no.nav.frivillig.skattetrekk.security

enum class OAuthTypes(
    val claimName: String,
    val authPrefix: String,
) {
    TOKENX("scope", "TOKEN_X_"),
}
