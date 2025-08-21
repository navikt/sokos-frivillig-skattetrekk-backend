package no.nav.frivillig.skattetrekk.configuration

import org.slf4j.Marker
import org.slf4j.MarkerFactory

val TEAM_LOGS_MARKER: Marker? = MarkerFactory.getMarker("TEAM_LOGS")

enum class AppId(
    val supportsTokenX: Boolean,
) {
    OPPDRAG_REST_PROXY(false),
}
