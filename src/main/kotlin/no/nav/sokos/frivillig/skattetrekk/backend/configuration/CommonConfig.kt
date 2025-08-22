package no.nav.sokos.frivillig.skattetrekk.backend.configuration

import org.slf4j.Marker
import org.slf4j.MarkerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

val TEAM_LOGS_MARKER: Marker? = MarkerFactory.getMarker("TEAM_LOGS")

enum class AppId(
    val supportsTokenX: Boolean,
) {
    OPPDRAG_REST_PROXY(false),
}

@Configuration
class TrekkConfig(
    @Value("\${trekk.endpoint.url}")
    val trekkUrl: String,
    @Value("\${trekk.scope}")
    val trekkScope: String,
    @Value("\${trekk.audience}")
    val audience: String,
)
