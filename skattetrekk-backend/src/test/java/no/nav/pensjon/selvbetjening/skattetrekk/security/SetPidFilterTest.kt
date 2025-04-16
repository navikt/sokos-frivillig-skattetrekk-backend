package no.nav.pensjon.selvbetjening.skattetrekk.security

import io.mockk.every
import jakarta.servlet.http.Cookie
import no.nav.pensjon.selvbetjening.skattetrekk.client.fullmakt.FullmaktClient
import no.nav.pensjon.selvbetjening.skattetrekk.client.fullmakt.RepresentasjonsforholdValidity
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest
class SetPidFilterTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var fullmaktClient: FullmaktClient

    @Mock
    private lateinit var tokenService: TokenService

    @Test
    fun `skal returnere allow access når fyldig fullmakt eksisterer`() {
        every { tokenService.determineTokenType() } returns TokenService.TokenType.TOKEN_X
        every { tokenService.determineRequestingPid() } returns "requestingPid"
        every { fullmaktClient.hasValidRepresentasjonsforhold("fullmaktsgiverPid", "requestingPid") } returns
                RepresentasjonsforholdValidity(true, "", "fullmaktsgiverFnr", "")

        mockMvc.perform(
            get("/api/skattetrekk")
                .header(HttpHeaders.AUTHORIZATION, "Bearer some-token")
                .cookie(Cookie("nav-obo", "fullmaktsgiverPid"))
                .with(jwt())
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `should return forbidden when no valid fullmakt exists`() {
        every { tokenService.determineTokenType() } returns TokenService.TokenType.TOKEN_X
        every { tokenService.determineRequestingPid() } returns "requestingPid"
        every { fullmaktClient.hasValidRepresentasjonsforhold("fullmaktsgiverPid", "requestingPid") } returns
                RepresentasjonsforholdValidity(true, "", "fullmaktsgiverFnr", "")

        mockMvc.perform(
            get("/api/skattetrekk")
                .header(HttpHeaders.AUTHORIZATION, "Bearer some-token")
                .cookie(Cookie("nav-obo", "fullmaktsgiverPid"))
                .with(jwt())
        )
            .andExpect(status().isForbidden)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("FORBIDDEN"))
    }

    @Test
    fun `should return forbidden when token type is invalid`() {
        every { tokenService.determineTokenType() } returns TokenService.TokenType.TOKEN_X

        mockMvc.perform(
            get("/api/skattetrekk")
                .header(HttpHeaders.AUTHORIZATION, "Bearer some-token")
                .with(jwt())
        )
            .andExpect(status().isForbidden)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("FORBIDDEN"))
    }
}