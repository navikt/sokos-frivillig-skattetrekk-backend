package no.nav.frivillig.skattetrekk.security

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.frivillig.skattetrekk.client.fullmakt.FullmaktClient
import no.nav.frivillig.skattetrekk.client.fullmakt.RepresentasjonsforholdValidity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.io.PrintWriter


class SetPidFilterTest {
    private val fullmaktClient = mock(FullmaktClient::class.java)
    private val tokenService = mock(TokenService::class.java)

    private val filter = SetPidFilter(fullmaktClient, tokenService)
    private val objectMapper = ObjectMapper()

    // Mock request objekt
    val request = mock(HttpServletRequest::class.java)

    @BeforeEach
    fun setupContext(){
        SecurityContextHolder.setContext(
            SecurityContextImpl(
                JwtAuthenticationToken(
                    Jwt(
                        "test",
                        null,
                        null,
                        mapOf("test" to "test"),
                        mapOf("test" to "test")
                    )
                )
            )
        )

        `when`(request.requestURI).thenReturn("/mocked/endpoint")
    }

    @Test
    fun `should set AuthenticatedUserDetails with fullmakt data when user logged in on behalf of other person and has valid fullmakt`(){
        val pidFullmektig = "00000000001"
        val pidFullmaktsgiver = "00000000002"

        val response = mock(HttpServletResponse::class.java)
        val filterChain = mock(FilterChain::class.java)

        `when`(request.method).thenReturn("GET")
        `when`(request.getHeader("Authorization")).thenReturn("Test")
        `when`(tokenService.determineTokenType()).thenReturn(TokenService.TokenType.TOKEN_X)
        `when`(tokenService.determineRequestingPid()).thenReturn(pidFullmektig)
        `when`(request.cookies).thenReturn(arrayOf(Cookie("nav-obo", pidFullmaktsgiver)))
        `when`(fullmaktClient.hasValidRepresentasjonsforhold("GET", pidFullmaktsgiver, pidFullmektig)).thenReturn(
            RepresentasjonsforholdValidity(true,null, "fnr_kryptert", "00000000002")
        )

        filter.doFilter(request, response, filterChain)

        assertTrue(SecurityContextUtil.isFullmakt())
        assertEquals(pidFullmaktsgiver, SecurityContextUtil.getPidFromContext())
    }

    @Test
    fun `should set AuthenticatedUserDetails with isFullmakt false when fullmaktsgiver equals requesting pid (acting on behalf of self)`(){
        val pidFullmaktsgiver = "00000000002"

        val response = mock(HttpServletResponse::class.java)
        val filterChain = mock(FilterChain::class.java)
        val writerMock = mock(PrintWriter::class.java)

        `when`(request.method).thenReturn("GET")
        `when`(response.writer).thenReturn(writerMock)
        `when`(request.getHeader("Authorization")).thenReturn("Test")
        `when`(tokenService.determineTokenType()).thenReturn(TokenService.TokenType.TOKEN_X)
        `when`(tokenService.determineRequestingPid()).thenReturn(pidFullmaktsgiver)
        `when`(request.cookies).thenReturn(arrayOf(Cookie("nav-obo", "fnr_kryptert")))
        `when`(fullmaktClient.hasValidRepresentasjonsforhold("GET", "fnr_kryptert", pidFullmaktsgiver)).thenReturn(
            RepresentasjonsforholdValidity(false, null, "fnr_kryptert", pidFullmaktsgiver)
        )

        filter.doFilter(request, response, filterChain)

        verify(writerMock, times(1)).write(anyString())
    }

    @Test
    fun `should set AuthenticatedUserDetails with isFullmakt false when no fullmakt cookie present`(){
        val pid = "00000000002"

        val response = mock(HttpServletResponse::class.java)
        val filterChain = mock(FilterChain::class.java)

        `when`(request.getHeader("Authorization")).thenReturn("Test")
        `when`(tokenService.determineTokenType()).thenReturn(TokenService.TokenType.TOKEN_X)
        `when`(tokenService.determineRequestingPid()).thenReturn(pid)

        filter.doFilter(request, response, filterChain)

        assertFalse(SecurityContextUtil.isFullmakt())
        assertEquals(pid, SecurityContextUtil.getPidFromContext())
    }

    @Test
    fun `should resolve to FORBIDDEN with VEILEDER_UNAUTHORIZED when user logged in on behalf of other person but lacks valid fullmakt`(){
        val pidFullmektig = "00000000001"
        val pidFullmaktsgiver = "00000000002"
        val path = "/random/endpoint"

        val response = MockHttpServletResponse()
        val filterChain = mock(FilterChain::class.java)

        `when`(request.method).thenReturn("GET")
        `when`(request.getHeader("Authorization")).thenReturn("Test")
        `when`(request.cookies).thenReturn(arrayOf(Cookie("nav-obo", pidFullmaktsgiver)))
        `when`(request.requestURI).thenReturn(path)
        `when`(tokenService.determineTokenType()).thenReturn(TokenService.TokenType.TOKEN_X)
        `when`(tokenService.determineRequestingPid()).thenReturn(pidFullmektig)
        `when`(fullmaktClient.hasValidRepresentasjonsforhold("GET", pidFullmaktsgiver, pidFullmektig)).thenReturn(
            RepresentasjonsforholdValidity(false, null, "fnr_kryptert", "12345678910")
        )

        filter.doFilter(request, response, filterChain)

        val errorResponse = objectMapper.readValue(response.contentAsString, SetPidFilterErrorResponse::class.java)

        assertEquals(ErrorCode.NO_FULLMAKT_PRESENT, errorResponse.message)
        assertEquals(HttpStatus.FORBIDDEN.value(), response.status)
        assertEquals(HttpStatus.FORBIDDEN.name, errorResponse.error)
        assertEquals(path, errorResponse.path)
    }

    @Test
    fun `should set AuthenticationDetails when user is logged in as saksbehandler and saksbehandler has access to user`(){
        val pid = "00000000001"

        val request = mock(HttpServletRequest::class.java)
        val response = mock(HttpServletResponse::class.java)
        val filterChain = mock(FilterChain::class.java)

        `when`(request.getHeader("Authorization")).thenReturn("Test")
        `when`(request.getHeader("pid")).thenReturn(pid)
        `when`(tokenService.determineTokenType()).thenReturn(TokenService.TokenType.AZURE_AD_ON_BEHALF_OF)
        `when`(tokenService.isUserInSkjermetGroup()).thenReturn(false)
        `when`(personService.hasSaksbehandlerAccessToPid(pid)).thenReturn(true)

        filter.doFilter(request, response, filterChain)

        assertFalse(SecurityContextUtil.isFullmakt())
        assertEquals(pid, SecurityContextUtil.getPidFromContext())
    }

    @Test
    fun `should set AuthenticationDetails  when user is logged in as saksbehandler with skjerming access and person is skjermet`(){
        val pid = "00000000001"

        val request = mock(HttpServletRequest::class.java)
        val response = mock(HttpServletResponse::class.java)
        val filterChain = mock(FilterChain::class.java)

        `when`(request.getHeader("Authorization")).thenReturn("Test")
        `when`(request.getHeader("pid")).thenReturn(pid)
        `when`(tokenService.determineTokenType()).thenReturn(TokenService.TokenType.AZURE_AD_ON_BEHALF_OF)
        `when`(tokenService.isUserInSkjermetGroup()).thenReturn(true)
        `when`(skjermingClient.isSkjermet(pid)).thenReturn(true)
        `when`(personService.hasSaksbehandlerAccessToPid(pid)).thenReturn(true)

        filter.doFilter(request, response, filterChain)

        assertFalse(SecurityContextUtil.isFullmakt())
        assertEquals(pid, SecurityContextUtil.getPidFromContext())
    }

    @Test
    fun `should resolve to FORBIDDEN with VEILEDER_UNAUTHORIZED when user is logged in as saksbehandler and user lacks access to skjermet person`(){
        val pid = "00000000001"
        val path = "/random/endpoint"

        val request = mock(HttpServletRequest::class.java)
        val response = MockHttpServletResponse()
        val filterChain = mock(FilterChain::class.java)

        `when`(request.getHeader("Authorization")).thenReturn("Test")
        `when`(request.getHeader("pid")).thenReturn(pid)
        `when`(request.requestURI).thenReturn(path)
        `when`(tokenService.determineTokenType()).thenReturn(TokenService.TokenType.AZURE_AD_ON_BEHALF_OF)
        `when`(tokenService.isUserInSkjermetGroup()).thenReturn(false)
        `when`(tokenService.isLoginLevelHigh()).thenReturn(false)
        `when`(skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when`(personService.hasAdressebeskyttelse(pid)).thenReturn(true)

        filter.doFilter(request, response, filterChain)

        val errorResponse = objectMapper.readValue(response.contentAsString, SetPidFilterErrorResponse::class.java)

        assertEquals(ErrorCode.VEILEDER_UNAUTHORIZED, errorResponse.message)
        assertEquals(HttpStatus.FORBIDDEN.value(), response.status)
        assertEquals(HttpStatus.FORBIDDEN.name, errorResponse.error)
        assertEquals(path, errorResponse.path)
    }

    @Test
    fun `should resolve to FORBIDDEN with VEILEDER_UNAUTHORIZED when user is logged in as saksbehandler and user lacks access to person`(){
        val pid = "00000000001"
        val path = "/random/endpoint"

        val request = mock(HttpServletRequest::class.java)
        val response = MockHttpServletResponse()
        val filterChain = mock(FilterChain::class.java)

        `when`(request.getHeader("Authorization")).thenReturn("Test")
        `when`(request.getHeader("pid")).thenReturn(pid)
        `when`(request.requestURI).thenReturn(path)
        `when`(tokenService.determineTokenType()).thenReturn(TokenService.TokenType.AZURE_AD_ON_BEHALF_OF)
        `when`(tokenService.isUserInSkjermetGroup()).thenReturn(true)
        `when`(personService.hasSaksbehandlerAccessToPid(pid)).thenReturn(false)

        filter.doFilter(request, response, filterChain)

        val errorResponse = objectMapper.readValue(response.contentAsString, SetPidFilterErrorResponse::class.java)

        assertEquals(ErrorCode.VEILEDER_UNAUTHORIZED, errorResponse.message)
        assertEquals(HttpStatus.FORBIDDEN.value(), response.status)
        assertEquals(HttpStatus.FORBIDDEN.name, errorResponse.error)
        assertEquals(path, errorResponse.path)
    }

    @Test
    fun `should resolve to FORBIDDEN with LOGIN_LEVEL_TOO_LOW when user has diskresjon and logged in with insufficient login level`(){
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
        `when`(tokenService.isUserInSkjermetGroup()).thenReturn(false)
        `when`(tokenService.isLoginLevelHigh()).thenReturn(false)
        `when`(personService.hasAdressebeskyttelse(pid)).thenReturn(true)

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
        `when`(tokenService.isUserInSkjermetGroup()).thenReturn(false)
        `when`(tokenService.isLoginLevelHigh()).thenReturn(true)
        `when`(personService.hasAdressebeskyttelse(pid)).thenReturn(true)

        filter.doFilter(request, response, filterChain)

        assertFalse(SecurityContextUtil.isFullmakt())
        assertEquals(pid, SecurityContextUtil.getPidFromContext())
    }

    @Test
    fun `should set AuthenticatedUserDetails when user is logged in with level3 and without diskresjonskode`() {
        val pid = "00000000001"

        val request = mock(HttpServletRequest::class.java)
        val response = mock(HttpServletResponse::class.java)
        val filterChain = mock(FilterChain::class.java)

        `when`(request.getHeader("Authorization")).thenReturn("Test")
        `when`(request.getHeader("pid")).thenReturn(pid)
        `when`(tokenService.determineTokenType()).thenReturn(TokenService.TokenType.TOKEN_X)
        `when`(tokenService.determineRequestingPid()).thenReturn(pid)
        `when`(tokenService.isUserInSkjermetGroup()).thenReturn(false)
        `when`(tokenService.isLoginLevelHigh()).thenReturn(false)
        `when`(personService.hasAdressebeskyttelse(pid)).thenReturn(false)

        filter.doFilter(request, response, filterChain)

        assertFalse(SecurityContextUtil.isFullmakt())
        assertEquals(pid, SecurityContextUtil.getPidFromContext())
    }

    @Test
    fun `should not allow fullmakt for users with diskresjon`() {
        val pidFullmektig = "00000000001"
        val pidFullmaktsgiver = "00000000002"

        val response = MockHttpServletResponse()
        val filterChain = mock(FilterChain::class.java)

        `when`(request.method).thenReturn("POST")
        `when`(request.getHeader("Authorization")).thenReturn("Test")
        `when`(request.getHeader("pid")).thenReturn(pidFullmektig)
        `when`(request.cookies).thenReturn(arrayOf(Cookie("nav-obo", pidFullmaktsgiver)))
        `when`(tokenService.determineTokenType()).thenReturn(TokenService.TokenType.TOKEN_X)
        `when`(tokenService.determineRequestingPid()).thenReturn(pidFullmektig)
        `when`(personService.hasAdressebeskyttelse(pidFullmaktsgiver)).thenReturn(true)
        `when`(fullmaktClient.hasValidRepresentasjonsforhold("POST", pidFullmaktsgiver, pidFullmektig)).thenReturn(
            RepresentasjonsforholdValidity(true, "En person", "fnr_kryptert", "00000000002")
        )

        filter.doFilter(request, response, filterChain)

        val errorResponse = objectMapper.readValue(response.contentAsString, SetPidFilterErrorResponse::class.java)

        assertEquals(ErrorCode.NO_FULLMAKT_PRESENT, errorResponse.message)
        assertEquals(HttpStatus.FORBIDDEN.value(), response.status)
        assertEquals(HttpStatus.FORBIDDEN.name, errorResponse.error)
    }
}