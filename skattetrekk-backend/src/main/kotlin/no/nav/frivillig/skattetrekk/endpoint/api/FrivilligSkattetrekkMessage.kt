package no.nav.frivillig.skattetrekk.endpoint.api

data class FrivilligSkattetrekkMessage(
    val messageCode: FrivilligSkattettrekkMessageCode,
    val details: String = messageCode.details,
    val type: FrivilligSkattetrekkType = messageCode.type,
    val metadata: Map<MetadataKey, Any?> = mapOf(),
)

enum class FrivilligSkattettrekkMessageCode(val type: FrivilligSkattetrekkType, val details: String) {
    OPPDRAG_SYSTEMET_ER_NEDE(FrivilligSkattetrekkType.ERROR, "Oppdragsystemet er nede eller utilgjengelig"),
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