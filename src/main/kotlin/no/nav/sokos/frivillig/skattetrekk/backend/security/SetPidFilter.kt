package no.nav.sokos.frivillig.skattetrekk.backend.security

import java.time.LocalDateTime

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

import no.nav.sokos.frivillig.skattetrekk.backend.controller.LogInLevelTooLowException
import no.nav.sokos.frivillig.skattetrekk.backend.controller.UnauthorizedException

@Profile("!test")
@Component
class SetPidFilter(
    private val tokenService: TokenService,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null) {
            try {
                if (tokenService.determineTokenType() != TokenService.TokenType.TOKEN_X) throw UnauthorizedException()
                if (!tokenService.isLoginLevelHigh()) throw LogInLevelTooLowException()

                val requestingPid = tokenService.determineRequestingPid()
                (SecurityContextHolder.getContext().authentication as JwtAuthenticationToken).details =
                    AuthenticatedUserDetails(requestingPid)

                filterChain.doFilter(request, response)
            } catch (e: Exception) {
                val path = request.requestURI
                when (e) {
                    is UnauthorizedException -> forbiddenResponse(response, ErrorCode.UNAUTHORIZED, path)
                    is LogInLevelTooLowException -> forbiddenResponse(response, ErrorCode.LOGIN_LEVEL_TOO_LOW, path)
                    else -> throw e
                }
            }
        } else {
            filterChain.doFilter(request, response) // Undersøk sikkerhet her - readiness kall
        }
    }

    private fun forbiddenResponse(
        response: HttpServletResponse,
        errorCode: ErrorCode,
        path: String,
    ) {
        val forbiddenStatus = HttpStatus.FORBIDDEN.value()
        val mapper = ObjectMapper()
        val errorResponse =
            SetPidFilterErrorResponse(
                timestamp = LocalDateTime.now().toString(),
                status = forbiddenStatus,
                error = HttpStatus.FORBIDDEN.name,
                message = errorCode,
                path = path,
            )
        response.apply {
            status = forbiddenStatus
            setHeader("Content-Type", "application/json")
            writer.write(mapper.writeValueAsString(errorResponse))
        }
    }
}
