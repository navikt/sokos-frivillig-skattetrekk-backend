package no.nav.sokos.frivillig.skattetrekk.backend.security

enum class OAuthTypes(
    val claimName: String,
    val authPrefix: String,
) {
    TOKENX("scope", "TOKEN_X_"),
}
