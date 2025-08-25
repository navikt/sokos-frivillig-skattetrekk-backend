package no.nav.sokos.frivillig.skattetrekk.backend.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import no.nav.sokos.frivillig.skattetrekk.backend.controller.models.FrivilligSkattetrekkInitResponse
import no.nav.sokos.frivillig.skattetrekk.backend.security.AuthenticatedUserDetails
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

    @BeforeEach
    fun setupSecurityContext() {
        val details = AuthenticatedUserDetails(pid = "00000000001")
        val auth = UsernamePasswordAuthenticationToken("user", "password")
        auth.details = details
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = auth
        SecurityContextHolder.setContext(context)
    }

    @Test
    fun `happy case`() {
        Mockito.`when`(skattetrekkService.hentSkattetrekk(anyString())).thenReturn(
            FrivilligSkattetrekkInitResponse(
                messages = emptyList(),
                data = null,
            ),
        )

        mockMvc
            .perform(
                get("/api/skattetrekk")
                    .header("authorization", "Bearer test-token"),
            ).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }
}
