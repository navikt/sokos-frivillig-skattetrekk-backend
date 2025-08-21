package no.nav.sokos.frivillig.skattetrekk.backend.endpoint

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

@Profile("test")
@Configuration
class MockSecurityConfig {
    @Bean
    fun mockSecurityContext(): SecurityContext {
        val jwt =
            Jwt
                .withTokenValue("test-token")
                .header("alg", "none")
                .claim("iss", "test-issuer")
                .claim("acr", "Level4")
                .claim("pid", "00000000001")
                .build()
        val auth: Authentication = JwtAuthenticationToken(jwt)
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = auth
        SecurityContextHolder.setContext(context)
        return context
    }
}
