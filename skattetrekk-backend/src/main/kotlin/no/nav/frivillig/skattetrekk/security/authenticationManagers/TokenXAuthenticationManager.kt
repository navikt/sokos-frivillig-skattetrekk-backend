package no.nav.frivillig.skattetrekk.security.authenticationManagers

import no.nav.frivillig.skattetrekk.security.ClaimTypes
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.stereotype.Component

@Component("tokenXAuthManager")
class TokenXAuthenticationManager(
    @Value("\${oauth2.tokenX.issuer}") private val tokenXIssuer: String,
    @Qualifier("jwtDecoderTokenX") jwtDecoderTokenX: NimbusJwtDecoder
): AuthenticationManager {

    val authProvider = JwtAuthenticationProvider(jwtDecoderTokenX).apply {
        this.setJwtAuthenticationConverter(JwtAuthenticationConverter().apply {
            this.setJwtGrantedAuthoritiesConverter(createJwtConverter())
        })
    }

    override fun authenticate(authentication: Authentication): Authentication =
        authProvider.authenticate(authentication)

    private fun createJwtConverter() = JwtGrantedAuthoritiesConverter().apply {
        this.setAuthoritiesClaimName(ClaimTypes.TOKENX.claimName)
        this.setAuthorityPrefix(ClaimTypes.TOKENX.authPrefix)
    }
}