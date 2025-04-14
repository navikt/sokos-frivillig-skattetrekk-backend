package no.nav.pensjon.selvbetjening.skattetrekk.hentskattogtrekk

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

abstract class WebClientTest {
    lateinit var server: MockWebServer
    lateinit var baseUrl: String

    @BeforeEach
    open fun setup() {
        server = MockWebServer()
        server.start()
        baseUrl = "http://localhost:${server.port}"
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    protected open fun prepare(response: MockResponse) {
        server.enqueue(response)
    }

    protected open fun takeRequest(): RecordedRequest {
        return server.takeRequest()
    }

    protected open fun jsonResponse(status: HttpStatus): MockResponse? {
        return jsonResponse().setResponseCode(status.value())
    }

    private fun jsonResponse(): MockResponse {
        return MockResponse().addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    }
}