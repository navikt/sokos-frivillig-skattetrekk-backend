package no.nav.frivillig.skattetrekk.endpoint.api

data class FrivilligSkattetrekkMessage(
    val details: String? = null,
    val type: FrivilligSkattetrekkType,
)

enum class FrivilligSkattetrekkType {
    ERROR,
    WARNING,
    INFO
}