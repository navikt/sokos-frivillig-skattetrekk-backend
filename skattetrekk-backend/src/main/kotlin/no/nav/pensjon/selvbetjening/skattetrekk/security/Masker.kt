package no.nav.pensjon.selvbetjening.skattetrekk.security
class Masker {
    companion object {
        val NORMAL_FNR_LENGTH = 11
        val END_INDEX_OF_BIRTH_DATE_PART_OF_FNR = 6

        fun maskPid(pid: String?): String {
            if (pid == null) {
                return "null"
            }
            return (
                    if (pid.length == NORMAL_FNR_LENGTH)
                        pid.substring(0, END_INDEX_OF_BIRTH_DATE_PART_OF_FNR) + "*****"
                    else
                        String.format("****** (length %d)", pid.length))
        }
    }
}