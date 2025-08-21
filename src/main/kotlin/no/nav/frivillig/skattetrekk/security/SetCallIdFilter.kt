package no.nav.frivillig.skattetrekk.security

import java.util.UUID

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

import no.nav.frivillig.skattetrekk.client.CallIdUtil.Companion.NAV_CALL_ID_NAME

@Component
class SetCallIdFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        MDC.put(NAV_CALL_ID_NAME, UUID.randomUUID().toString())
        filterChain.doFilter(request, response)
    }
}
