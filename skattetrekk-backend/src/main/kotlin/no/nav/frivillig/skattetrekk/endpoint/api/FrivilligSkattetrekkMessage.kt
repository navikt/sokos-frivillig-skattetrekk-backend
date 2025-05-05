package no.nav.frivillig.skattetrekk.endpoint.api

data class FrivilligSkattetrekkMessage(
    val messageCode: FrivilligSkattettrekkMessageCode,
    val details: String = messageCode.details,
    val type: FrivilligSkattetrekkType = messageCode.type,
    val metadata: Map<MetadataKey, Any?> = mapOf(),
)

enum class FrivilligSkattettrekkMessageCode(val type: FrivilligSkattetrekkType, val details: String) {
    USER_HAS_NO_TREKKVEDTAK(FrivilligSkattetrekkType.ERROR, "Test message"),
}

enum class FrivilligSkattetrekkType {
    ERROR,
    WARNING,
    INFO
}

enum class FieldReference {
    TEST
}

enum class MetadataKey {
    AFFECTED_FIELD
}