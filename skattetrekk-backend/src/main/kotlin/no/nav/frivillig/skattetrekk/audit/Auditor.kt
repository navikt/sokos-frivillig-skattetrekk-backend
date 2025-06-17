package no.nav.frivillig.skattetrekk.audit

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import org.springframework.stereotype.Component
import java.time.ZonedDateTime


/**
 * Sends info to the auditing system when an internal user (NAV employee) or a fullmektig
 * performs some task on behalf of a regular (external) user.
 * The info is formatted according to ArcSight CEF (Common Event Format).
 */
@Component
class Auditor {

    fun auditFullmaktRead(fullmektigPid: String, onBehalfOfPid: String) {
        audit.info(
            cefEntryRead(
                fullmektigPid,
                "Fullmektig",
                onBehalfOfPid,
                "audit:read"
            ).format())
    }

    fun auditFullmaktCreate(fullmektigPid: String, onBehalfOfPid: String) {
        audit.info(
            cefEntryRead(
                fullmektigPid,
                "Fullmektig",
                onBehalfOfPid,
                "audit:create"
            ).format())
    }

    companion object {
        private val audit: Logger = LoggerFactory.getLogger("AUDIT_LOGGER")

        private fun cefEntryRead(userId: String, userType: String, onBehalfOfPid: String, event: String): CefEntry {
            return CefEntry(
                ZonedDateTime.now().toInstant().toEpochMilli(),
                Level.INFO,
                event,
                "Datahenting paa vegne av",
                "$userType henter data for innbygger",
                userId,
                onBehalfOfPid
            )
        }
        private fun cefEntryCreate(userId: String, userType: String, onBehalfOfPid: String, event: String): CefEntry {
            return CefEntry(
                ZonedDateTime.now().toInstant().toEpochMilli(),
                Level.INFO,
                event,
                "Oppretter data paa vegne av",
                "$userType endrer skattetrekk for innbygger",
                userId,
                onBehalfOfPid
            )
        }
    }
}
