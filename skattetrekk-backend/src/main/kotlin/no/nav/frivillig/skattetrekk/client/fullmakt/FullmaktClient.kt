package no.nav.frivillig.skattetrekk.client.fullmakt

import no.nav.pensjon.selvbetjening.skattetrekk.configuration.AppId
import no.nav.pensjon.selvbetjening.skattetrekk.security.Masker
import no.nav.pensjon.selvbetjening.skattetrekk.security.AzureAdService
import no.nav.pensjon.selvbetjening.skattetrekk.security.TokenService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder

@Component
class FullmaktClient(
    @Value("\${fullmakt.endpoint.url}") private val baseUrl: String,
    @Value("\${fullmakt.scope}") private val scope: String,
    @Value("\${fullmakt.audience}") private val audience: String,
    private val webClient: WebClient,
    private val tokenService: TokenService
) {

    fun hasValidRepresentasjonsforhold(fullmaktsgiverPid: String, fullmektigPid: String): RepresentasjonsforholdValidity? {
        return try {
            tokenService.getEgressToken(scope, audience, fullmektigPid, AppId.PENSJON_FULLMAKT).let {
                webClient
                    .get()
                    .uri(url())
                    .headers { headers: HttpHeaders ->
                        headers.setBearerAuth(it!!)
                        headers[HttpHeaders.CONTENT_TYPE] = MediaType.APPLICATION_JSON_VALUE
                        headers[HttpHeaders.ACCEPT] = MediaType.APPLICATION_JSON_VALUE
                        headers[NAV_CALL_ID] = MDC.get(NAV_CALL_ID)
                        headers[FULLMAKTSGIVER_PID] = fullmaktsgiverPid
                    }
                    .retrieve()
                    .bodyToMono(RepresentasjonsforholdValidity::class.java)
                    .block()
            }

        } catch (e: WebClientResponseException) {
            logger.error("Kall til fullmaktstjenesten feilet med melding: ${e.responseBodyAsString}")
            throw FullmaktException(
                SERVICE,
                "hasValidRepresentasjonsforhold",
                "Failed to call service: " + e.responseBodyAsString,
                e
            )
        } catch (e: ResponseStatusException) {
            logger.error("Kall til fullmaktstjenesten feilet med statuskode ${e.statusCode}: ${e.message}")
            throw FullmaktException(SERVICE, "hasValidRepresentasjonsforhold", "Failed to call service", e)
        } catch (e: RuntimeException) { // e.g. when connection broken
            logger.error("Kall til fullmaktstjenesten feilet: ${e.message}")
            throw FullmaktException(SERVICE, "hasValidRepresentasjonsforhold", "Failed to call service", e)
        }
    }

    private fun url(): String {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/representasjon/hasValidRepresentasjonsforhold")
            .queryParam(VALID_REPRESENTASJONSTYPER_KEY, VALID_REPRESENTASJONSTYPER)
            .build()
            .toUriString()
    }

    companion object {
        private const val SERVICE = "Fullmakt"

        const val NAV_CALL_ID = "Nav-Call-Id"

        const val VALID_REPRESENTASJONSTYPER_KEY = "validRepresentasjonstyper"
        private val VALID_REPRESENTASJONSTYPER = setOf("PENSJON_LES", "PENSJON_VERGE", "PENSJON_VERGE_PENGEMOTTAKER", "PENSJON_SKRIV")
        const val FULLMAKTSGIVER_PID = "fullmaktsgiverPid"

        private val logger: Logger = LoggerFactory.getLogger(FullmaktClient::class.java)

    }
}

