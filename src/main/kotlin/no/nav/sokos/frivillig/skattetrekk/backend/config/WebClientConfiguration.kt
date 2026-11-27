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
        createProxyHttpClient { System.getenv("HTTP_PROXY") }

    @Bean("trekkHttpClient", destroyMethod = "close")
    fun trekkHttpClient(): KtorHttpClient =
        createTrekkHttpClient()
}

fun createProxyHttpClient(proxyUrl: () -> String?): KtorHttpClient =
    KtorHttpClient(CIO) {
        expectSuccess = true
        engine {
            proxyUrl()?.let { proxy = ProxyBuilder.http(Url(it)) }
        }
        install(ContentNegotiation) { jackson() }
    }

fun createTrekkHttpClient(): KtorHttpClient =
    KtorHttpClient(CIO) {
        expectSuccess = true
        install(ContentNegotiation) { jackson() }
    }
