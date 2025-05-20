package no.nav.frivillig.skattetrekk.endpoint.api

data class FrivilligSkattetrekkMessage(
    val details: FrivilligSkattetrekkMessageDetail? = null,
    val type: FrivilligSkattetrekkType,
)

enum class FrivilligSkattetrekkMessageDetail(val message: String) {
    OPPDRAG_UTILGJENGELIG("Oppdragssystemet er nede eller utilgjengelig"),
}

enum class FrivilligSkattetrekkType {
    ERROR,
    WARNING,
    INFO
}