package no.nav.sokos.frivillig.skattetrekk.backend.client.trekk

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutputStreamContent
import io.ktor.http.headersOf
import io.ktor.serialization.jackson3.jackson
import io.ktor.utils.io.InternalAPI
import io.mockk.every
import io.mockk.mockk
import no.nav.sokos.frivillig.skattetrekk.backend.config.AppId
import no.nav.sokos.frivillig.skattetrekk.backend.controller.ClientException
import no.nav.sokos.frivillig.skattetrekk.backend.security.TokenService
import no.nav.sokos.frivillig.skattetrekk.backend.service.TrekkTypeCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class TrekkClientTest {
    private val tokens = mockk<TokenService> {
        every { getEgressToken("scope", "audience", "123", AppId.OPPDRAG_REST_PROXY) } returns "token"
    }

    @Test
    @OptIn(InternalAPI::class)
    fun `sends authenticated json and reads list`() {
        HttpClient(MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("https://trekk.example/api/nav-tjeneste-trekk/finnTrekkListe", request.url.toString())
            assertEquals("Bearer token", request.headers[HttpHeaders.Authorization])
            assertEquals(ContentType.Application.Json, request.body.contentType?.withoutParameters())
            val output = ByteArrayOutputStream()
            (request.body as OutputStreamContent).writeTo(output)
            val json = output.toString(Charsets.UTF_8)
            assertTrue(json.contains("123"))
            assertTrue(json.contains("FRIS"))
            respond("""{"trekkInfoListe":[]}""", headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }) {
            expectSuccess = true
            install(ContentNegotiation) { jackson() }
        }.use {
            assertEquals(emptyList<Any>(), client(it).finnTrekkListe("123", TrekkTypeCode.FRIS))
        }
    }

    @Test
    fun `empty responses produce empty list or null`() {
        HttpClient(MockEngine { respond("", HttpStatusCode.OK) }) {
            expectSuccess = true
            install(ContentNegotiation) { jackson() }
        }.use {
            assertEquals(emptyList<Any>(), client(it).finnTrekkListe("123", TrekkTypeCode.FRIS))
            assertNull(client(it).hentSkattOgTrekk("123", 42))
        }
    }

    @Test
    fun `http and transport failures map to client exception`() {
        HttpClient(MockEngine { respond("failure", HttpStatusCode.InternalServerError) }) {
            expectSuccess = true
            install(ContentNegotiation) { jackson() }
        }.use {
            val error = assertThrows(ClientException::class.java) { client(it).hentSkattOgTrekk("123", 42) }
            assertEquals("trekk-api", error.service)
            assertTrue(!error.message!!.contains("Failed to fetch trekkliste"))
        }
        HttpClient(MockEngine { throw IllegalStateException("network") }) {
            install(ContentNegotiation) { jackson() }
        }.use {
            val error = assertThrows(ClientException::class.java) { client(it).hentSkattOgTrekk("123", 42) }
            assertTrue(error.message!!.contains("Failed to fetch trekkliste: network"))
        }
    }

    private fun client(http: HttpClient) = TrekkClient("https://trekk.example", "scope", "audience", tokens, http)
}