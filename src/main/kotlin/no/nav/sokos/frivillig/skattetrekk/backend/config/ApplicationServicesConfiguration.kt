package no.nav.sokos.frivillig.skattetrekk.backend.config

import io.ktor.client.HttpClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.TrekkClient
import no.nav.sokos.frivillig.skattetrekk.backend.security.TokenService
import no.nav.sokos.frivillig.skattetrekk.backend.service.BehandleTrekkService
import no.nav.sokos.frivillig.skattetrekk.backend.service.HentSkattOgTrekkService

@Configuration
class ApplicationServicesConfiguration {
    @Bean
    fun trekkClient(
        @Value("\${trekk.endpoint.url}") url: String,
        @Value("\${trekk.scope}") scope: String,
        @Value("\${trekk.audience}") audience: String,
        tokenService: TokenService,
        @Qualifier("trekkHttpClient") httpClient: HttpClient,
    ) = TrekkClient(url, scope, audience, tokenService, httpClient)

    @Bean
    fun hentSkattOgTrekkService(trekkClient: TrekkClient) = HentSkattOgTrekkService(trekkClient)

    @Bean
    fun behandleTrekkService(trekkClient: TrekkClient) = BehandleTrekkService(trekkClient)
}