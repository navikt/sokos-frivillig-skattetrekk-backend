package no.nav.frivillig.skattetrekk.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.frivillig.skattetrekk.client.fullmakt.FullmaktClient
import no.nav.frivillig.skattetrekk.client.fullmakt.api.RepresentasjonsforholdValidity
import no.nav.frivillig.skattetrekk.configuration.AppId
import no.nav.frivillig.skattetrekk.endpoint.NoFullmaktPresentException
import no.nav.frivillig.skattetrekk.endpoint.UnauthorizedException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.LocalDateTime

@Component
class SetPidFilter(
    private val fullmaktClient: FullmaktClient,
    private val tokenService: TokenService
): OncePerRequestFilter() {

    private val log: Logger = LoggerFactory.getLogger(SetPidFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null) {
            try {
                if (tokenService.determineTokenType() != TokenService.TokenType.TOKEN_X) {
                    throw UnauthorizedException()
                }

                val navOnBehalfOfCookie = request.cookies?.firstOrNull { cookie -> cookie.name.equals("nav-obo") }
                val authenticatedUserDetails = checkBorgerTilgang(request.method, navOnBehalfOfCookie)
                (SecurityContextHolder.getContext().authentication as JwtAuthenticationToken).details =
                    authenticatedUserDetails

                filterChain.doFilter(request, response)
            } catch (e: Exception) {
                val path = request.requestURI
                when (e) {
                    is NoFullmaktPresentException -> forbiddenResponse(response, ErrorCode.NO_FULLMAKT_PRESENT, path)
                    is UnauthorizedException -> forbiddenResponse(response, ErrorCode.UNAUTHORIZED, path)
                    else -> throw e
                }
            }
        } else {
            filterChain.doFilter(request, response) // Undersøk sikkerhet her - readiness kall
        }
    }

    private fun forbiddenResponse(response: HttpServletResponse, errorCode: ErrorCode, path: String
    ) {
        val forbiddenStatus = HttpStatus.FORBIDDEN.value()
        val mapper = ObjectMapper()
        val errorResponse = SetPidFilterErrorResponse(
            timestamp = LocalDateTime.now().toString(),
            status = forbiddenStatus,
            error = HttpStatus.FORBIDDEN.name,
            message = errorCode,
            path = path
        )
        response.apply {
            status = forbiddenStatus
            setHeader("Content-Type", "application/json")
            writer.write(mapper.writeValueAsString(errorResponse))
        }
    }

    fun checkBorgerTilgang(httpMethod: String, navOnBehalfOfCookie: Cookie?) : AuthenticatedUserDetails {
        val requestingPid = tokenService.determineRequestingPid()
        if (navOnBehalfOfCookie != null) {
            val fullmaktsgiverKryptertPid = navOnBehalfOfCookie.value
            val representasjonsforholdValidity = haandterFullmakt(fullmaktsgiverKryptertPid, requestingPid)
            return AuthenticatedUserDetails(representasjonsforholdValidity.fullmaktsgiverFnr, representasjonsforholdValidity.hasValidRepresentasjonsforhold)
        }

        return AuthenticatedUserDetails(requestingPid, false)
    }

    private fun haandterFullmakt(fullmaktsgiverPid: String, requestingPid: String): RepresentasjonsforholdValidity {
        val harGyldigFullmakt = fullmaktClient.hasValidRepresentasjonsforhold(fullmaktsgiverPid, requestingPid)
        if (!harGyldigFullmakt?.hasValidRepresentasjonsforhold!!) {
            log.info("Fullmaktsforhold er ikke funnet. Nekter adgang")
            throw NoFullmaktPresentException(AppId.PENSJON_FULLMAKT.name, "", "", null)
        }

        /*
        TODO fix me
        if(personService.hasAdressebeskyttelse(harGyldigFullmakt.fullmaktsgiverFnr)) {
            log.info("Fullmaktsforhold for bruker med adressebeskyttelse. Nekter adgang")
            throw NoFullmaktPresentException()
        }

         */

        return harGyldigFullmakt
    }

}