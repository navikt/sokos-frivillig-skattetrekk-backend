package no.nav.sokos.frivillig.skattetrekk.backend.controller

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import no.nav.sokos.frivillig.skattetrekk.backend.controller.models.FrivilligSkattetrekkInitResponse
import no.nav.sokos.frivillig.skattetrekk.backend.security.RequestContext
import no.nav.sokos.frivillig.skattetrekk.backend.service.BehandleTrekkService
import no.nav.sokos.frivillig.skattetrekk.backend.service.HentSkattOgTrekkService

@ActiveProfiles("test")
@WebMvcTest(SkattetrekkController::class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MockSecurityConfig::class)
class SkattetrekkControllerTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockitoBean
    lateinit var skattetrekkService: HentSkattOgTrekkService

    @MockitoBean
    lateinit var behandleTrekkService: BehandleTrekkService


    @Test
    fun `happy case`() {
        Mockito.`when`(skattetrekkService.hentSkattetrekk(any(RequestContext::class.java) ?: RequestContext("", null, null, ""))).thenReturn(
            FrivilligSkattetrekkInitResponse(
                messages = emptyList(),
                data = null,
            ),
        )

        mockMvc
            .perform(
                get("/api/skattetrekk")
                    .header("authorization", "Bearer test-token")
                    .principal(JwtAuthenticationToken(Jwt.withTokenValue("test-token").header("alg", "RS256").subject("00000000001").build())),
            ).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }
}
