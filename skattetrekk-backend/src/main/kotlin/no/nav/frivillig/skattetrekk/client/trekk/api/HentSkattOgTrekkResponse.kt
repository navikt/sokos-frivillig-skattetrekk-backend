package no.nav.frivillig.skattetrekk.client.trekk.api

data class HentSkattOgTrekkResponse(
    val andreTrekk: AndreTrekkResponse?,
    val skattetrekk: Skattetrekk?,
)