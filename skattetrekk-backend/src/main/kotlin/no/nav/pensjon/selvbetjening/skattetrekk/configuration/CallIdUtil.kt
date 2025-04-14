package no.nav.pensjon.selvbetjening.skattetrekk.configuration

import org.slf4j.MDC

class CallIdUtil {
    companion object {
        const val NAV_CALL_ID_NAME = "Nav-Call-Id"
    }
}

fun CallIdUtil.Companion.getCallIdFromMdc() = MDC.get(NAV_CALL_ID_NAME)