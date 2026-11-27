package no.nav.sokos.frivillig.skattetrekk.backend.security

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Date
import no.nav.sokos.frivillig.skattetrekk.backend.controller.skattetrekkModule
import no.nav.sokos.frivillig.skattetrekk.backend.controller.ClientException
import no.nav.sokos.frivillig.skattetrekk.backend.controller.PersonNotFoundException
import no.nav.sokos.frivillig.skattetrekk.backend.controller.UnauthorizedException
import no.nav.sokos.frivillig.skattetrekk.backend.service.BehandleTrekkService
import no.nav.sokos.frivillig.skattetrekk.backend.service.HentSkattOgTrekkService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SecurityBoundaryIntegrationTest {
    private val keys = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
    private val otherKeys = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
    private val issuer = "https://issuer.example"
    private val provider = mockk<JwkProvider>()
    private val jwk = mockk<Jwk>()
    private val hent = mockk<HentSkattOgTrekkService>()
    private val behandle = mockk<BehandleTrekkService>(relaxed = true)
    private val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    private fun token(issuer: String = this.issuer, expires: Long = 300, wrongKey: Boolean = false): String {
        val pair = if (wrongKey) otherKeys else keys
        return JWT.create().withKeyId("key").withIssuer(issuer).withSubject("12345678901")
            .withClaim("acr", "Level4").withExpiresAt(Date(System.currentTimeMillis() + expires * 1000))
            .sign(Algorithm.RSA256(pair.public as RSAPublicKey, pair.private as RSAPrivateKey))
    }

    @Test
    fun `verifies JWT and maps request context`() = testApplication {
        every { provider.get("key") } returns jwk
        every { jwk.publicKey } returns keys.public
        val captured = slot<RequestContext>()
        every { hent.hentSkattetrekk(capture(captured)) } returns null
        application { skattetrekkModule(hent, behandle, issuer, provider, registry) }
        val token = token()
        assertEquals(HttpStatusCode.OK, client.get("/api/skattetrekk") { header(HttpHeaders.Authorization, "Bearer $token") }.status)
        assertEquals("12345678901", captured.captured.pid)
        assertEquals(issuer, captured.captured.issuer)
        assertEquals("Level4", captured.captured.acr)
        assertEquals(token, captured.captured.bearerToken)
    }

    @Test
    fun `rejects missing invalid issuer signature and expired tokens`() = testApplication {
        every { provider.get("key") } returns jwk
        every { jwk.publicKey } returns keys.public
        application { skattetrekkModule(hent, behandle, issuer, provider, registry) }
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/skattetrekk").status)
        val wrongAlgorithm = JWT.create().withKeyId("key").withIssuer(issuer).withSubject("12345678901")
            .sign(Algorithm.RSA384(keys.public as RSAPublicKey, keys.private as RSAPrivateKey))
        for (invalid in listOf(token(wrongKey = true), token(issuer = "https://wrong.example"), token(expires = -120), wrongAlgorithm)) {
            assertEquals(HttpStatusCode.Unauthorized, client.get("/api/skattetrekk") { header(HttpHeaders.Authorization, "Bearer $invalid") }.status)
        }
        verify(exactly = 0) { hent.hentSkattetrekk(any()) }
    }

    @Test
    fun `health metrics and validation routes`() = testApplication {
        every { provider.get("key") } returns jwk
        every { jwk.publicKey } returns keys.public
        application { skattetrekkModule(hent, behandle, issuer, provider, registry) }
        assertEquals(HttpStatusCode.OK, client.get("/actuator/health").status)
        assertEquals(HttpStatusCode.OK, client.get("/actuator/prometheus").status)
        val response = client.post("/api/skattetrekk") {
            header(HttpHeaders.Authorization, "Bearer ${token()}")
            contentType(ContentType.Application.Json)
            setBody("{\"value\":-1,\"satsType\":\"KRONER\"}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        verify(exactly = 0) { behandle.behandleTrekk(any(), any(), any()) }
        val valid = client.post("/api/skattetrekk") {
            header(HttpHeaders.Authorization, "Bearer ${token(expires = -30)}")
            contentType(ContentType.Application.Json)
            setBody("{\"value\":10,\"satsType\":\"KRONER\"}")
        }
        assertEquals(HttpStatusCode.OK, valid.status)
        verify(exactly = 1) { behandle.behandleTrekk(any(), 10, any()) }
    }

    @Test
    fun `maps service exceptions to their HTTP statuses`() = testApplication {
        every { provider.get("key") } returns jwk
        every { jwk.publicKey } returns keys.public
        application { skattetrekkModule(hent, behandle, issuer, provider, registry) }
        val authorization = "Bearer ${token()}"
        every { hent.hentSkattetrekk(any()) } throws PersonNotFoundException("pdl", "person", null, null)
        assertEquals(HttpStatusCode.BadRequest, client.get("/api/skattetrekk") { header(HttpHeaders.Authorization, authorization) }.status)
        every { hent.hentSkattetrekk(any()) } throws ClientException("system", "service", null, null)
        assertEquals(HttpStatusCode.ServiceUnavailable, client.get("/api/skattetrekk") { header(HttpHeaders.Authorization, authorization) }.status)
        every { hent.hentSkattetrekk(any()) } throws UnauthorizedException()
        assertEquals(HttpStatusCode.Forbidden, client.get("/api/skattetrekk") { header(HttpHeaders.Authorization, authorization) }.status)
    }
}
