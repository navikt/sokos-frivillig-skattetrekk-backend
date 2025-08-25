package no.nav.sokos.frivillig.skattetrekk.backend.controller.models

data class FrivilligSkattetrekkMessage(
    val code: FrivilligSkattetrekkMessageCode? = null,
    val type: FrivilligSkattetrekkType,
)

enum class FrivilligSkattetrekkMessageCode(
    val message: String,
) {
    OPPDRAG_UTILGJENGELIG("Oppdragssystemet er nede eller utilgjengelig"),
    MAX_BELOP_OVERSTEGET("Maksimalt beløp for frivillig skattetrekk er 1000 kr"),
    MAX_PROSENT_OVERSTEGET("Maksimalt prosent for frivillig skattetrekk er 50%"),
    MIN_BELOP("Beløpet må være større enn 0"),
    MIN_PROSENT("Prosentrekket kan ikke være mindre enn 0"),
    OPPHØR_REGISTRERT("Opphør av frivillig skattetrekk er registrert"),
}

enum class FrivilligSkattetrekkType {
    ERROR,
    WARNING,
    INFO,
}
