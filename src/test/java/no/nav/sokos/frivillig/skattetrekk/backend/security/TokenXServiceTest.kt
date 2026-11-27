package no.nav.sokos.frivillig.skattetrekk.backend.security

import java.time.Instant
import java.util.UUID

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.SignedJWT
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.http.parseQueryString
import io.ktor.serialization.jackson3.jackson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TokenXServiceTest {
    private val endpoint = "https://token.example.org/token"
    private val clientId = "client-id"
    private val key = RSAKeyGenerator(2048).keyID("test-key").generate()

    @Test
    fun `exchanges token with signed assertion and form parameters`() {
        val before = Instant.now().epochSecond
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals(endpoint, request.url.toString())
            val body = request.body as OutgoingContent.ByteArrayContent
            assertEquals(ContentType.Application.FormUrlEncoded, body.contentType?.withoutParameters())
            val form = parseQueryString(body.bytes().toString(Charsets.UTF_8))
            assertEquals(6, form.names().size)
            assertEquals(TokenXService.GRANT_TYPE, form[TokenXService.PARAMS_GRANT_TYPE])
            assertEquals(TokenXService.SUBJECT_TOKEN_TYPE, form[TokenXService.PARAMS_SUBJECT_TOKEN_TYPE])
            assertEquals("subject +/& token", form[TokenXService.PARAMS_SUBJECT_TOKEN])
            assertEquals("audience +/& value", form[TokenXService.PARAMS_AUDIENCE])
            assertEquals(TokenXService.CLIENT_ASSERTION_TYPE, form[TokenXService.PARAMS_CLIENT_ASSERTION_TYPE])

            val assertion = SignedJWT.parse(form[TokenXService.PARAMS_CLIENT_ASSERTION])
            assertEquals(JWSAlgorithm.RS256, assertion.header.algorithm)
            assertEquals(JOSEObjectType.JWT, assertion.header.type)
            assertEquals("test-key", assertion.header.keyID)
            assertTrue(assertion.verify(RSASSAVerifier(key.toRSAPublicKey())))
            val claims = assertion.jwtClaimsSet
            assertEquals(clientId, claims.subject)
            assertEquals(clientId, claims.issuer)
            assertEquals(listOf(endpoint), claims.audience)
            assertNotNull(UUID.fromString(claims.jwtid))
            assertTrue(claims.issueTime.toInstant().epochSecond >= before)
            assertEquals(claims.issueTime, claims.notBeforeTime)
            assertTrue(claims.expirationTime.toInstant().epochSecond >= claims.issueTime.toInstant().epochSecond + 30)
            assertTrue(claims.expirationTime.toInstant().epochSecond <= Instant.now().epochSecond + 30)
            respond(
                """{"access_token":"egress","token_type":"Bearer","expires_in":120,"extra":"ignored"}""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        client(engine).use { client ->
            assertEquals("egress", service(client).exchangeIngressTokenToEgressToken("subject +/& token", "audience +/& value"))
        }
    }

    @Test
    fun `returns null for an empty response`() {
        client(MockEngine { respond("", HttpStatusCode.OK) }).use { client ->
            assertNull(service(client).exchangeIngressTokenToEgressToken("subject", "audience"))
        }
    }

    @Test
    fun `throws on client and server errors`() {
        client(MockEngine { respond("bad request", HttpStatusCode.BadRequest) }).use { client ->
            val exception = assertThrows(ClientRequestException::class.java) {
                service(client).exchangeIngressTokenToEgressToken("subject", "audience")
            }
            assertEquals(HttpStatusCode.BadRequest, exception.response.status)
        }
        client(MockEngine { respond("server error", HttpStatusCode.InternalServerError) }).use { client ->
            val exception = assertThrows(ServerResponseException::class.java) {
                service(client).exchangeIngressTokenToEgressToken("subject", "audience")
            }
            assertEquals(HttpStatusCode.InternalServerError, exception.response.status)
        }
    }

    private fun client(engine: MockEngine) = HttpClient(engine) {
        install(ContentNegotiation) { jackson() }
    }

    private fun service(client: HttpClient) = TokenXService(endpoint, key.toJSONString(), clientId, client)
}