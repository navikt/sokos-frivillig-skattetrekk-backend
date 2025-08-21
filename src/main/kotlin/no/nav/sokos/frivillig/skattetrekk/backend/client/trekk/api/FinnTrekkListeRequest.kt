package no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api

class FinnTrekkListeRequest(
    val debitorSok: DebitorSok? = null,
    val kreditorSok: KreditorSok? = null,
)
