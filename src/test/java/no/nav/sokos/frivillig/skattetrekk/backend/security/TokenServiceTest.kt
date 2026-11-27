package no.nav.sokos.frivillig.skattetrekk.backend.security

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

import no.nav.sokos.frivillig.skattetrekk.backend.config.AppId

class TokenServiceTest {
    private val tokenXService = mockk<TokenXService>()
    private val azureAdService = mockk<AzureAdService>()
    private val service = TokenService("issuer", tokenXService, azureAdService)

    @Test
    fun `uses client credentials for the configured downstream application`() {
        val context = RequestContext("pid", "issuer", "Level4", "ingress-token")
        every { azureAdService.retrieveClientCredentialsToken(listOf("scope")) } returns "egress-token"

        assertEquals("egress-token", service.getEgressToken("scope", "audience", context, AppId.OPPDRAG_REST_PROXY))
        assertEquals(Innloggingstype.LEVEL4, service.getInnloggingstype(context))
        verify(exactly = 0) { tokenXService.exchangeIngressTokenToEgressToken(any(), any()) }
        assertEquals("RequestContext([redacted])", context.toString())
    }

    @Test
    fun `rejects an unknown issuer`() {
        val context = RequestContext("pid", "other", null, "ingress-token")
        assertThrows(CouldNotDetermineTokenTypeException::class.java) {
            service.getEgressToken("scope", "audience", context, AppId.OPPDRAG_REST_PROXY)
        }
    }
}