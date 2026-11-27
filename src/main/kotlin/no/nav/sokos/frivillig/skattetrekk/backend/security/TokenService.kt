package no.nav.sokos.frivillig.skattetrekk.backend.security

import java.lang.IllegalStateException

import no.nav.sokos.frivillig.skattetrekk.backend.config.AppId

class TokenService(
    private val tokenXIssuer: String,
    private val tokenXService: TokenXService,
    private val azureAdService: AzureAdService,
) {

    enum class TokenType {
        AZURE_AD_CLIENT_CREDENTIALS,
        TOKEN_X,
    }

    fun getEgressToken(
        scope: String,
        audience: String? = null,
        context: RequestContext,
        appId: AppId,
    ): String? =
        context.let {
            val scopes = listOf(scope)

            when (typeOf(context, appId)) {
                TokenType.TOKEN_X ->
                    audience?.let { audience ->
                        tokenXService.exchangeIngressTokenToEgressToken(context.bearerToken, audience)
                    } ?: throw EgressAudienceMissingException()
                TokenType.AZURE_AD_CLIENT_CREDENTIALS -> azureAdService.retrieveClientCredentialsToken(scopes)
            }
        }

    fun determineTokenType(context: RequestContext): TokenType {
        if (context.issuer == tokenXIssuer) {
            return TokenType.TOKEN_X
        }
        throw IllegalStateException("Unknown token type")
    }

    fun getInnloggingstype(context: RequestContext): Innloggingstype {
        context.let {
            val acr = context.acr
            if ("Level4" == acr || "idporten-loa-high" == acr) {
                return Innloggingstype.LEVEL4
            }
            if ("Level3" == acr || "idporten-loa-substantial" == acr) {
                return Innloggingstype.LEVEL3
            }
        }
        return Innloggingstype.SYSTEM
    }

    private fun typeOf(
        context: RequestContext,
        appId: AppId,
    ): TokenType {
        val issuer = context.issuer
        if (issuer == tokenXIssuer) {
            if (appId.supportsTokenX) {
                return TokenType.TOKEN_X
            } else {
                return TokenType.AZURE_AD_CLIENT_CREDENTIALS
            }
        }
        throw CouldNotDetermineTokenTypeException()
    }
}

enum class Innloggingstype {
    LEVEL4,
    LEVEL3,
    SYSTEM,
}

class EgressAudienceMissingException : RuntimeException("Audience missing when scoping token for outgoing call")

class CouldNotDetermineTokenTypeException : RuntimeException("Unable to determine type of token")
