package no.nav.sokos.frivillig.skattetrekk.backend.security

import org.springframework.security.core.context.SecurityContextHolder

class SecurityContextUtil {
    companion object {
        fun getPidFromContext(): String = (SecurityContextHolder.getContext().authentication.details as AuthenticatedUserDetails).pid
    }
}
