package no.nav.frivillig.skattetrekk.client.fullmakt

import no.nav.frivillig.skattetrekk.client.fullmakt.api.RepresentasjonsforholdValidity
import no.nav.frivillig.skattetrekk.configuration.AppId
import no.nav.frivillig.skattetrekk.endpoint.ClientException
import no.nav.frivillig.skattetrekk.endpoint.ForbiddenException
import no.nav.frivillig.skattetrekk.endpoint.PersonNotFoundException
import no.nav.frivillig.skattetrekk.security.TokenService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
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
            when(e.statusCode) {
                HttpStatus.FORBIDDEN -> throw ForbiddenException(AppId.PENSJON_FULLMAKT.name, FULLMAKT_API, "Ikke tilgang til fullmakt-api", null)
                HttpStatus.NOT_FOUND -> throw PersonNotFoundException(AppId.PENSJON_FULLMAKT.name, FULLMAKT_API, "Ressurs ikke funnet", null)
                else -> throw ClientException(AppId.PENSJON_FULLMAKT.name, FULLMAKT_API, "Intern feil fra fullmakt-api ved sjekk om fullmakt", null)
            }
        } catch (e: Exception) {
            throw ClientException(AppId.PENSJON_FULLMAKT.name, FULLMAKT_API, "Intern feil fra fullmakt-api ved sjekk om fullmakt", e)
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
        private const val FULLMAKT_API = "fullmakt-api"
        const val NAV_CALL_ID = "Nav-Call-Id"

        const val VALID_REPRESENTASJONSTYPER_KEY = "validRepresentasjonstyper"
        private val VALID_REPRESENTASJONSTYPER = setOf("PENSJON_LES", "PENSJON_VERGE", "PENSJON_VERGE_PENGEMOTTAKER", "PENSJON_SKRIV")
        const val FULLMAKTSGIVER_PID = "fullmaktsgiverPid"

        private val logger: Logger = LoggerFactory.getLogger(FullmaktClient::class.java)

    }
}

