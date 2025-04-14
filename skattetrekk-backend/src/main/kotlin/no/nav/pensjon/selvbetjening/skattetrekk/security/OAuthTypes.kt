package no.nav.pensjon.selvbetjening.skattetrekk.security

/*
 * Enum som beskriver navn på claims i azure ad og maskinporten og tilhørende prefix vi bruker i spring security(authPrefix)
*/
enum class ClaimTypes(val claimName: String, val authPrefix: String) {
    AZURE_ROLE("roles", "AZURE_ROLE_"),
    AZURE_SCOPE( "scope", "AZURE_SCOPE_"),
    AZURE_SCP( "scp", "AZURE_SCOPE_"),
    AZURE_GROUPS( "groups", "AZURE_GROUP_"),
    TOKENX( "scope", "TOKEN_X_")
}

val azureAdClaimTypes = listOf(ClaimTypes.AZURE_ROLE, ClaimTypes.AZURE_SCOPE, ClaimTypes.AZURE_SCP, ClaimTypes.AZURE_GROUPS)

class OAuthException(message: String): Exception(message)
class OAuthUnauthorizedException: Exception()