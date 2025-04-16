package no.nav.frivillig.skattetrekk.client.util

import no.nav.pensjon.selvbetjening.skattetrekk.security.Masker
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class Errorhandler {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Errorhandler::class.java)

        fun handleResponseStatusException(
            statusCode: HttpStatus,
            pid: String,
            e: Throwable? = null,
            message: String? = null,
        ): ResponseStatusException {
            val failedResponseMessage =
                "Request failed with status: $statusCode ${message?.let { "and message: $it " } ?: ""}for pid ${
                    Masker.maskPid(pid)
                }. NAV-Call-ID: ${CallIdUtil.getCallIdFromMdc()}"
            when (statusCode) {
                HttpStatus.INTERNAL_SERVER_ERROR -> logger.error(failedResponseMessage, e)
                else -> logger.warn(failedResponseMessage, e)
            }

            return ResponseStatusException(statusCode, failedResponseMessage)
        }
    }
}