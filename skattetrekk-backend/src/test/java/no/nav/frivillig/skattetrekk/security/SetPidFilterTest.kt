package no.nav.frivillig.skattetrekk.security

import io.mockk.every
import jakarta.servlet.http.Cookie
import no.nav.pensjon.selvbetjening.skattetrekk.client.fullmakt.FullmaktClient
import no.nav.pensjon.selvbetjening.skattetrekk.client.fullmakt.RepresentasjonsforholdValidity
import no.nav.pensjon.selvbetjening.skattetrekk.service.BehandleTrekkService
import no.nav.pensjon.selvbetjening.skattetrekk.service.HentSkattOgTrekkService
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
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
    @InjectMocks
    private lateinit var hentSkattOgTrekkService: HentSkattOgTrekkService
    @InjectMocks
    private lateinit var behandleTrekkService: BehandleTrekkService

    @Test
    fun `skal tillate tilgang når gyldig fullmakt eksisterer`() {
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
    fun `skal returnere forbidden når det ikke er gyldig tilgang`() {
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
    fun `sakl returnere forbidden når tokenet er ugyldig`() {
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