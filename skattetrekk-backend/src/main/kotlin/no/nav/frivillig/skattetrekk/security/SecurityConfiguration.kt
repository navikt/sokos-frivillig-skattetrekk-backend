package no.nav.frivillig.skattetrekk.security

import jakarta.servlet.DispatcherType
import no.nav.frivillig.skattetrekk.security.authenticationManagers.AzureAdAuthenticationManager
import no.nav.frivillig.skattetrekk.security.authenticationManagers.TokenXAuthenticationManager
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.DispatcherTypeRequestMatcher
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Configuration
@EnableWebSecurity
class SecurityConfiguration(
    @Value("\${oauth2.tokenX.issuer}") private val tokenXIssuer: String,
    @Value("\${oauth2.tokenX.jsonWebKeyUri}") private val tokenXJsonWebKeyUri: String
) {

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity, jwtIssuerAuthenticationManagerResolver: JwtIssuerAuthenticationManagerResolver): SecurityFilterChain? {
        http {
            oauth2ResourceServer {
                authenticationManagerResolver = jwtIssuerAuthenticationManagerResolver
            }
            authorizeHttpRequests {
                authorize("/actuator/**", permitAll)
                authorize(DispatcherTypeRequestMatcher(DispatcherType.ERROR), authenticated)
                authorize("/api/**", authenticated)
                authorize(anyRequest, denyAll)
            }
        }
        return http.build()
    }

    @Bean
    fun jwtIssuerAuthenticationManagerResolver(
        @Qualifier("azureAdAuthManager") azureAuthManager: AzureAdAuthenticationManager,
        @Qualifier("tokenXAuthManager") tokenXAuthManager: TokenXAuthenticationManager
        ) =
        JwtIssuerAuthenticationManagerResolver { issuer: String ->
            when (issuer) {
                tokenXIssuer -> tokenXAuthManager
                else -> throw RuntimeException()
            }
        }

    @Bean("jwtDecoderTokenX")
    fun jwtDecoderTokenX(): NimbusJwtDecoder {
        val jwtDecoder = NimbusJwtDecoder
            .withJwkSetUri(tokenXJsonWebKeyUri)
            .build()

        jwtDecoder.setJwtValidator(
            DelegatingOAuth2TokenValidator(
                JwtTimestampValidator(Duration.ofSeconds(60)),
                JwtIssuerValidator(tokenXIssuer),
            )
        )
        return jwtDecoder
    }
}

