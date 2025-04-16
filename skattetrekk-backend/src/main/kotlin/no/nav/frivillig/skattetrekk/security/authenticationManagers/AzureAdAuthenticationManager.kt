package no.nav.frivillig.skattetrekk.security.authenticationManagers

import no.nav.frivillig.skattetrekk.security.ClaimTypes
import no.nav.frivillig.skattetrekk.security.azureAdClaimTypes
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

@Primary
@Component("azureAdAuthManager")
class AzureAdAuthenticationManager (
    @Value("\${oauth2.azureAd.issuer}") private val azureAdIssuer: String,
    @Value("\${oauth2.azureAd.clientId}") private val azureAdAudience: String
): AuthenticationManager {

    val authProvider = JwtAuthenticationProvider(createJwtDecoder()).apply {
        this.setJwtAuthenticationConverter(JwtAuthenticationConverter().apply {
            this.setJwtGrantedAuthoritiesConverter(AzureAdJwtGrantedAuthoritiesConverter())
        })
    }

    override fun authenticate(authentication: Authentication): Authentication =
        authProvider.authenticate(authentication)

    private fun createJwtDecoder() = JwtDecoders.fromIssuerLocation<NimbusJwtDecoder>(azureAdIssuer)
        .apply {
            this.setJwtValidator(createTokenValidator())
        }

    private fun createTokenValidator(): OAuth2TokenValidator<Jwt> {
        val issuerValidator = JwtValidators.createDefaultWithIssuer(azureAdIssuer)
        val audienceValidator =
            OAuth2TokenValidator { token: Jwt ->
                if (token.audience.any { azureAdAudience.contains(it) }) {
                    OAuth2TokenValidatorResult.success()
                } else {
                    OAuth2TokenValidatorResult.failure(
                        OAuth2Error("invalid_token",
                            String.format("None of required audience values '%s' found in token", azureAdAudience), null)
                    )
                }
            }
        return DelegatingOAuth2TokenValidator(issuerValidator, audienceValidator)
    }
}

class AzureAdJwtGrantedAuthoritiesConverter : Converter<Jwt, Collection<GrantedAuthority>> {

    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val grantedAuthorities = mutableListOf<GrantedAuthority>()
        getClaims(jwt).forEach { claim ->
            claim.values.forEach { value ->
                grantedAuthorities.add(SimpleGrantedAuthority("${claim.type.authPrefix}$value"))
            }
        }
        return grantedAuthorities
    }

    private fun getClaims(jwt: Jwt): Collection<JwtAuthClaim> =
        azureAdClaimTypes.map { authClaimType ->
            jwt.getClaim<Any>(authClaimType.claimName).let { claim ->
                if (claim is String) {
                    JwtAuthClaim(authClaimType, if (StringUtils.hasText(claim)) claim.split(" ") else emptyList())
                } else {
                    JwtAuthClaim(authClaimType, claim as? Collection<String> ?: emptyList())
                }
            }
        }
}

private data class JwtAuthClaim(val type: ClaimTypes, val values: Collection<String>)