package no.nav.sokos.frivillig.skattetrekk.backend.security

import org.springframework.security.core.context.SecurityContextHolder

class SecurityContextUtil {
    companion object {
        fun getPidFromContext(): String {
            val authentication =
                SecurityContextHolder.getContext().authentication
                    ?: throw IllegalStateException("No authentication in security context")

            val details =
                authentication.details as? AuthenticatedUserDetails
                    ?: throw IllegalStateException("Authentication details is not AuthenticatedUserDetails")

            return details.pid
        }
    }
}
