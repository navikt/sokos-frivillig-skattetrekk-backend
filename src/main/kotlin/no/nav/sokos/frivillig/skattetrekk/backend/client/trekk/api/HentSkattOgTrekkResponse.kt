package no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api

data class HentSkattOgTrekkResponse(
    val andreTrekk: AndreTrekkResponse?,
    val skattetrekk: Skattetrekk?,
)
