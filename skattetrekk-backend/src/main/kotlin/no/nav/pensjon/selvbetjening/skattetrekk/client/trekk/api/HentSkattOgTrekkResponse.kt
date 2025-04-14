package no.nav.pensjon.selvbetjening.skattetrekk.client.trekk.api

data class HentSkattOgTrekkResponse(
    val andreTrekk: AndreTrekkResponse?,
    val skattetrekk: Skattetrekk?,
)