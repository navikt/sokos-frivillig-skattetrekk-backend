package no.nav.sokos.frivillig.skattetrekk.backend.security

import java.time.Instant
import java.util.Base64

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import no.nav.sokos.frivillig.skattetrekk.backend.controller.SkattetrekkController
import no.nav.sokos.frivillig.skattetrekk.backend.security.authenticationManagers.TokenXAuthenticationManager
import no.nav.sokos.frivillig.skattetrekk.backend.service.BehandleTrekkService
import no.nav.sokos.frivillig.skattetrekk.backend.service.HentSkattOgTrekkService

@ActiveProfiles("security-boundary")
@WebMvcTest(SkattetrekkController::class)
@AutoConfigureMockMvc
@Import(SecurityConfiguration::class, TokenXAuthenticationManager::class, TokenService::class)
@TestPropertySource(properties = ["oauth2.tokenX.issuer=https://issuer.example", "oauth2.tokenX.jsonWebKeyUri=https://issuer.example/jwks"])
class SecurityBoundaryIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockitoBean(name = "jwtDecoderTokenX")
    lateinit var decoder: NimbusJwtDecoder

    @MockitoBean
    lateinit var skattetrekkService: HentSkattOgTrekkService

    @MockitoBean
    lateinit var behandleTrekkService: BehandleTrekkService

    @MockitoBean
    lateinit var tokenXService: TokenXService

    @MockitoBean
    lateinit var azureAdService: AzureAdService

    private val issuer = "https://issuer.example"
    private val pid = "12345678901"
    private val acr = "Level4"

    private fun token(): String {
        val encoder = Base64.getUrlEncoder().withoutPadding()
        fun encode(value: String) = encoder.encodeToString(value.toByteArray(Charsets.UTF_8))
        return "${encode("{\"alg\":\"RS256\"}")}.${encode("{\"iss\":\"$issuer\",\"sub\":\"$pid\"}")}.${encode("signature")}"
    }

    @Test
    fun `missing bearer token is rejected before reaching service`() {
        mockMvc.perform(get("/api/skattetrekk"))
            .andExpect(status().isUnauthorized)

        Mockito.verifyNoInteractions(decoder, skattetrekkService)
    }

    @Test
    fun `invalid bearer token is rejected before reaching service`() {
        val token = token()
        Mockito.`when`(decoder.decode(token)).thenThrow(BadJwtException("Invalid signature"))

        mockMvc.perform(get("/api/skattetrekk").header("Authorization", "Bearer $token"))
            .andExpect(status().isUnauthorized)

        Mockito.verify(decoder).decode(token)
        Mockito.verifyNoInteractions(skattetrekkService)
    }

    @Test
    fun `verified bearer token reaches service as request context`() {
        val token = token()
        val jwt = Jwt.withTokenValue(token)
            .header("alg", "RS256")
            .claim("iss", issuer)
            .subject(pid)
            .claim("acr", acr)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(300))
            .build()
        Mockito.`when`(decoder.decode(token)).thenReturn(jwt)

        mockMvc.perform(get("/api/skattetrekk").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)

        Mockito.verify(decoder).decode(token)
        val invocation = Mockito.mockingDetails(skattetrekkService).invocations.single()
        assertEquals("hentSkattetrekk", invocation.method.name)
        val context = invocation.getArgument<RequestContext>(0)
        assertEquals(pid, context.pid)
        assertEquals(issuer, context.issuer)
        assertEquals(acr, context.acr)
        assertEquals(token, context.bearerToken)
    }
}