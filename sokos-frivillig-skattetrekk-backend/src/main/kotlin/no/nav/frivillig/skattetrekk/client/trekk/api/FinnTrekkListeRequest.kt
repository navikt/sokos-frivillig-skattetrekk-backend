package no.nav.frivillig.skattetrekk.client.trekk.api

class FinnTrekkListeRequest(
    val debitorSok: DebitorSok? = null,
    val kreditorSok: KreditorSok? = null,
)
