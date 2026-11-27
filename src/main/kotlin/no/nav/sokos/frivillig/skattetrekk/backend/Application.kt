package no.nav.sokos.frivillig.skattetrekk.backend

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import java.net.URI

import no.nav.sokos.frivillig.skattetrekk.backend.config.ApplicationConfig
import no.nav.sokos.frivillig.skattetrekk.backend.config.ApplicationServicesConfiguration
import no.nav.sokos.frivillig.skattetrekk.backend.config.createProxyHttpClient
import no.nav.sokos.frivillig.skattetrekk.backend.config.createTrekkHttpClient
import no.nav.sokos.frivillig.skattetrekk.backend.controller.skattetrekkModule
import no.nav.sokos.frivillig.skattetrekk.backend.security.AzureAdService
import no.nav.sokos.frivillig.skattetrekk.backend.security.TokenService
import no.nav.sokos.frivillig.skattetrekk.backend.security.TokenXService

fun main() {
    val config = ApplicationConfig.fromEnvironment()
    val proxyClient = createProxyHttpClient { config.httpProxy }
    val trekkHttpClient = createTrekkHttpClient()
    try {
        val azure = AzureAdService(config.azureAd.clientId, config.azureAd.clientSecret, config.azureAd.tokenEndpoint, proxyClient)
        val tokenX = TokenXService(config.tokenX.tokenEndpoint, config.tokenX.privateJwk, config.tokenX.clientId, proxyClient)
        val tokens = TokenService(config.tokenX.issuer, tokenX, azure)
        val services = ApplicationServicesConfiguration()
        val trekk = services.trekkClient(config.trekk.url, config.trekk.scope, config.trekk.audience, tokens, trekkHttpClient)
        val jwks = JwkProviderBuilder(URI(config.tokenX.jsonWebKeyUri).toURL()).build()
        val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
        try {
            embeddedServer(Netty, configure = {
                connector { port = (System.getenv("SERVER_PORT") ?: "8080").toInt() }
                maxHeaderSize = 16 * 1024
            }, module = {
                skattetrekkModule(services.hentSkattOgTrekkService(trekk), services.behandleTrekkService(trekk), config.tokenX.issuer, jwks, registry)
            }).start(wait = true)
        } finally {
            registry.close()
        }
    } finally {
        trekkHttpClient.close()
        proxyClient.close()
    }
}
