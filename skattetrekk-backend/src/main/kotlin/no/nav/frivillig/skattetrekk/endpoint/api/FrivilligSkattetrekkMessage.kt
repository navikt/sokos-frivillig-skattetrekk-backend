package no.nav.frivillig.skattetrekk.endpoint.api

data class FrivilligSkattetrekkMessage(
    val messageCode: FrivilligSkattettrekkMessageCode,
    val details: String? = null,
    val type: FrivilligSkattetrekkType = messageCode.type,
)

enum class FrivilligSkattettrekkMessageCode(val type: FrivilligSkattetrekkType, val details: String) {
    OPPDRAG_SYSTEMET_ER_NEDE(FrivilligSkattetrekkType.ERROR, "Oppdragsystemet er nede eller utilgjengelig"),
}

enum class FrivilligSkattetrekkType {
    ERROR,
    WARNING,
    INFO
}