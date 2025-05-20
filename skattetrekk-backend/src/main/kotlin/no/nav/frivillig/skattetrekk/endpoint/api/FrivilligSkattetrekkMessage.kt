package no.nav.frivillig.skattetrekk.endpoint.api

data class FrivilligSkattetrekkMessage(
    val details: FrivilligSkattetrekkMessageDetail? = null,
    val type: FrivilligSkattetrekkType,
)

enum class FrivilligSkattetrekkMessageDetail(val message: String) {
    OPPDRAG_UTILGJENGELIG("Oppdragssystemet er nede eller utilgjengelig"),
    MAX_BELOP_OVERSTEGET("Maksimalt beløp for frivillig skattetrekk er 1000 kr"),
    MAX_PROSENT_OVERSTEGET("Maksimalt prosent for frivillig skattetrekk er 50%"),
    MIN_BELOP("Beløpet må være større enn 0"),
    MIN_PROSENT("Prosentrekket kan ikke være mindre enn 0"),
}

enum class FrivilligSkattetrekkType {
    ERROR,
    WARNING,
    INFO
}