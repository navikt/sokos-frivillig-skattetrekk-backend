package no.nav.sokos.frivillig.skattetrekk.backend.configuration

import java.net.URI

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.ProxyProvider

@Configuration
class WebClientConfiguration {
    @Bean("webClientProxy")
    fun webClientProxy(): WebClient =
        (
            System
                .getenv("HTTP_PROXY")
                ?.let { URI(it) }
                ?.run {
                    WebClient
                        .builder()
                        .clientConnector(
                            ReactorClientHttpConnector(
                                HttpClient
                                    .create()
                                    .proxy {
                                        it
                                            .type(ProxyProvider.Proxy.HTTP)
                                            .host(host)
                                            .port(port)
                                    },
                            ),
                        ).build()
                }
                ?: WebClient.builder().build()
        )

    @Bean
    fun webClient(): WebClient =
        WebClient
            .builder()
            .clientConnector(ReactorClientHttpConnector(HttpClient.create()))
            .exchangeStrategies(ExchangeStrategies.builder().codecs { it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }.build())
            .build()
}
