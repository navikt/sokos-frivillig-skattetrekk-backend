package no.nav.pensjon.selvbetjening.skattetrekk.security

import no.nav.pensjon.selvbetjening.skattetrekk.configuration.AppId
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import java.lang.IllegalStateException

@Service
class TokenService(
    @Value("\${oauth2.azureAd.issuer}") private val azureAdIssuer: String,
    @Value("\${oauth2.tokenX.issuer}") private val tokenXIssuer: String,
    private val azureAdService: AzureAdService,
    private val tokenXService: TokenXService,
) {

    enum class TokenType {
        AZURE_AD_CLIENT_CREDENTIALS, TOKEN_X
    }

    fun getEgressToken(scope: String, audience: String? = null, pid: String, appId: AppId): String? =
        SecurityContextHolder.getContext().authentication.let {
            val token = (it as JwtAuthenticationToken).token
            val scopes = listOf(scope)

            when (typeOf(token, pid, appId)) {
                TokenType.AZURE_AD_CLIENT_CREDENTIALS -> azureAdService.retrieveClientCredentialsToken(scopes)
                TokenType.TOKEN_X -> audience?.let { audience ->
                    tokenXService.exchangeIngressTokenToEgressToken(token.tokenValue, audience)
                } ?: throw EgressAudienceMissingException()
            }
        }


    fun isLoginLevelHigh(): Boolean = getInnloggingstype() == Innloggingstype.LEVEL4

    fun determineTokenType(): TokenType {
        SecurityContextHolder.getContext().authentication.let {
            val token = (it as JwtAuthenticationToken).token
            val issuer = token.getClaim<String>("iss")
            if (issuer == azureAdIssuer) {
                if (token.getClaim<String>("sub") == token.getClaim<String>("oid")) {
                    return TokenType.AZURE_AD_CLIENT_CREDENTIALS
                }
            } else if (issuer == tokenXIssuer) {
                return TokenType.TOKEN_X
            }
            throw IllegalStateException("Unknown token type")
        }
    }

    fun determineRequestingPid(): String {
        SecurityContextHolder.getContext().authentication.let {
            if (determineTokenType() == TokenType.TOKEN_X) {
                return it.name
            }
            return ""
        }
    }

    fun getInnloggingstype(): Innloggingstype {
        SecurityContextHolder.getContext().authentication.let {
            val token = (it as JwtAuthenticationToken).token
            val acr = token.getClaim<String>("acr")
            if ("Level4" == acr || "idporten-loa-high" == acr) {
                return Innloggingstype.LEVEL4
            }
            if ("Level3" == acr || "idporten-loa-substantial" == acr) {
                return Innloggingstype.LEVEL3
            }
        }
        return Innloggingstype.SYSTEM
    }

    private fun typeOf(jwt: Jwt, pid: String, appId: AppId): TokenType {
        val issuer = jwt.getClaim<String>("iss")
        if (issuer == azureAdIssuer) {
            if (jwt.getClaim<String>("sub") == jwt.getClaim<String>("oid")) {
                return TokenType.AZURE_AD_CLIENT_CREDENTIALS
            }
        } else if (issuer == tokenXIssuer) {

            if (appId.supportsTokenX) {

                val pidFromToken = jwt.getClaim<String>("pid")
                val isFullmaktToken = pid != pidFromToken

                if (isFullmaktToken) {
                    return if (appId.supportsFullmakt) TokenType.TOKEN_X else TokenType.AZURE_AD_CLIENT_CREDENTIALS
                }
                return TokenType.TOKEN_X
            }

            return TokenType.AZURE_AD_CLIENT_CREDENTIALS
        }
        throw CouldNotDetermineTokenTypeException()
    }
}

enum class Innloggingstype {
    LEVEL4,
    LEVEL3,
    SYSTEM
}

class EgressAudienceMissingException : RuntimeException("Audience missing when scoping token for outgoing call")
class CouldNotDetermineTokenTypeException : RuntimeException("Unable to determine type of token")