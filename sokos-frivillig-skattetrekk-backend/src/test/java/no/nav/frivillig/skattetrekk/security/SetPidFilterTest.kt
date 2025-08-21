package no.nav.frivillig.skattetrekk.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class SetPidFilterTest {
    private val tokenService = mock(TokenService::class.java)

    private val filter = SetPidFilter(tokenService)
    private val objectMapper = ObjectMapper()

    // Mock request objekt
    val request = mock(HttpServletRequest::class.java)

    @BeforeEach
    fun setupContext() {
        SecurityContextHolder.setContext(
            SecurityContextImpl(
                JwtAuthenticationToken(
                    Jwt(
                        "test",
                        null,
                        null,
                        mapOf("test" to "test"),
                        mapOf("test" to "test"),
                    ),
                ),
            ),
        )

        `when`(request.requestURI).thenReturn("/mocked/endpoint")
    }

    @Test
    fun `should resolve to FORBIDDEN with LOGIN_LEVEL_TOO_LOW when user is logged in with insufficient login level`() {
        val pid = "00000000001"
        val path = "/random/endpoint"

        val request = mock(HttpServletRequest::class.java)
        val response = MockHttpServletResponse()
        val filterChain = mock(FilterChain::class.java)

        `when`(request.getHeader("Authorization")).thenReturn("Test")
        `when`(request.getHeader("pid")).thenReturn(pid)
        `when`(request.requestURI).thenReturn(path)
        `when`(tokenService.determineTokenType()).thenReturn(TokenService.TokenType.TOKEN_X)
        `when`(tokenService.determineRequestingPid()).thenReturn(pid)
        `when`(tokenService.isLoginLevelHigh()).thenReturn(false)

        filter.doFilter(request, response, filterChain)

        val errorResponse = objectMapper.readValue(response.contentAsString, SetPidFilterErrorResponse::class.java)

        assertEquals(ErrorCode.LOGIN_LEVEL_TOO_LOW, errorResponse.message)
        assertEquals(HttpStatus.FORBIDDEN.value(), response.status)
        assertEquals(HttpStatus.FORBIDDEN.name, errorResponse.error)
        assertEquals(path, errorResponse.path)
    }

    @Test
    fun `should set AuthenticatedUserDetails when user has diskresjon and is logged in with sufficient login level`() {
        val pid = "00000000001"

        val request = mock(HttpServletRequest::class.java)
        val response = mock(HttpServletResponse::class.java)
        val filterChain = mock(FilterChain::class.java)

        `when`(request.getHeader("Authorization")).thenReturn("Test")
        `when`(request.getHeader("pid")).thenReturn(pid)
        `when`(tokenService.determineTokenType()).thenReturn(TokenService.TokenType.TOKEN_X)
        `when`(tokenService.determineRequestingPid()).thenReturn(pid)
        `when`(tokenService.isLoginLevelHigh()).thenReturn(true)

        filter.doFilter(request, response, filterChain)

        assertEquals(pid, SecurityContextUtil.getPidFromContext())
    }
}
