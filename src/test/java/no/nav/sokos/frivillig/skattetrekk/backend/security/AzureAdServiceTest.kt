package no.nav.sokos.frivillig.skattetrekk.backend.security

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
import io.ktor.http.parseUrlEncodedParameters
import io.ktor.serialization.jackson3.jackson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AzureAdServiceTest {
    private val endpoint = "https://login.example.com/token"

    @Test
    fun `submits encoded credentials and scopes and decodes token`() {
        val client = HttpClient(MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals(endpoint, request.url.toString())
            assertEquals(ContentType.Application.FormUrlEncoded, request.body.contentType?.withoutParameters())
            val form = (request.body as OutgoingContent.ByteArrayContent).bytes().decodeToString().parseUrlEncodedParameters(plusIsSpace = true)
            assertEquals(setOf("grant_type", "client_id", "client_secret", "scope"), form.names())
            assertEquals(listOf("client_credentials"), form.getAll("grant_type"))
            assertEquals(listOf("client+id"), form.getAll("client_id"))
            assertEquals(listOf("secret &+="), form.getAll("client_secret"))
            assertEquals(listOf("scope/a scope+b"), form.getAll("scope"))
            respond(
                """{"access_token":"decoded-token","extra":"ignored"}""",
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }) {
            install(ContentNegotiation) { jackson() }
        }
        client.use {
            assertEquals("decoded-token", AzureAdService("client+id", "secret &+=", endpoint, it).retrieveClientCredentialsToken(listOf("scope/a", "scope+b")))
        }
    }

    @Test
    fun `returns null for an empty response`() {
        HttpClient(MockEngine { respond("", HttpStatusCode.OK) }).use {
            assertNull(AzureAdService("id", "secret", endpoint, it).retrieveClientCredentialsToken(emptyList()))
        }
    }

    @Test
    fun `throws for client and server errors`() {
        for ((status, exception) in listOf(
            HttpStatusCode.BadRequest to ClientRequestException::class.java,
            HttpStatusCode.InternalServerError to ServerResponseException::class.java,
        )) {
            HttpClient(MockEngine { respond("failure", status) }).use {
                assertThrows(exception) { AzureAdService("id", "secret", endpoint, it).retrieveClientCredentialsToken(emptyList()) }
            }
        }
    }

    @Test
    fun `throws for malformed token response`() {
        val client = HttpClient(MockEngine {
            respond("not json", headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        }) {
            install(ContentNegotiation) { jackson() }
        }
        client.use {
            assertThrows(Exception::class.java) { AzureAdService("id", "secret", endpoint, it).retrieveClientCredentialsToken(emptyList()) }
        }
    }
}