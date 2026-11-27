package no.nav.sokos.frivillig.skattetrekk.backend.config

import io.ktor.client.HttpClient as KtorHttpClient
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.Url
import io.ktor.serialization.jackson3.jackson
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WebClientConfiguration {
    @Bean("webClientProxy", destroyMethod = "close")
    fun webClientProxy(): KtorHttpClient =
        KtorHttpClient(CIO) {
            expectSuccess = true
            engine {
                System.getenv("HTTP_PROXY")?.let { proxy = ProxyBuilder.http(Url(it)) }
            }
            install(ContentNegotiation) { jackson() }
        }

    @Bean("trekkHttpClient", destroyMethod = "close")
    fun trekkHttpClient(): KtorHttpClient =
        KtorHttpClient(CIO) {
            expectSuccess = true
            install(ContentNegotiation) { jackson() }
        }
}
