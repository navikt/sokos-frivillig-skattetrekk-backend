package no.nav.frivillig.skattetrekk.audit

import org.slf4j.event.Level


/**
 * CEF = ArcSight Common Event Format
 * Note that no character escaping is performed here, since the logged info does not require this.
 */
class CefEntry(
    private val timestamp: Long,
    private val level: Level,
    private val deviceEventClassId: String,
    private val name: String,
    private val message: String,
    private val sourceUserId: String,
    private val destinationUserId: String
) {
    fun format(): String {
        val elements = listOf(
            PREAMBLE + CEF_VERSION,
            DEVICE_VENDOR,
            DEVICE_PRODUCT,
            DEVICE_VERSION,
            deviceEventClassId,
            name,
            severity(),
            extension()
        )

        return java.lang.String.join(SEPARATOR, elements)
    }

    private fun severity(): String {
        return if (level == Level.INFO) "INFO" else "WARN"
    }

    /**
     * Note that it is recommended by #tech-logg_analyse_og_datainnsikt
     * to use 'msg' rather than 'act', since the latter has max. length 63
     */
    private fun extension(): String {
        return "end=" + timestamp +
                " suid=" + sourceUserId +
                " duid=" + destinationUserId +
                " msg=" + message +
                " flexString1Label=Decision" +
                " flexString1=Permit"
    }

    companion object {
        private const val CEF_VERSION = 0
        private const val PREAMBLE = "CEF:"
        private const val SEPARATOR = "|"
        private const val DEVICE_VENDOR = "Okonomi"
        private const val DEVICE_PRODUCT = "Frivillig skattetrekk"
        private const val DEVICE_VERSION = "1.0"
    }
}
