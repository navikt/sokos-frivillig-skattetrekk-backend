package no.nav.frivillig.skattetrekk.client.norg2

import no.nav.frivillig.skattetrekk.client.norg2.api.NavEnhetResponse
import no.nav.frivillig.skattetrekk.client.pdl.PdlClient.Companion.PDL_API
import no.nav.frivillig.skattetrekk.configuration.AppId
import no.nav.frivillig.skattetrekk.endpoint.ClientException
import no.nav.frivillig.skattetrekk.endpoint.ForbiddenException
import no.nav.frivillig.skattetrekk.security.TokenService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class Norg2Client(
    @Value("\${norg2.endpoint.url}") private val norg2Url: String,
    @Value("\${norg2.scope}") private val norg2Scope: String,
    @Value("\${norg2.audience}") private val norg2Audience: String,
    private val tokenService: TokenService,
    private val webClient: WebClient,
) {

    fun hentEnhetForSpesifisertGeografiskOmraade(pid: String, geografiskOmraade: String, diskresjonskode: String): String {
        try {
            return webClient
                .get()
                .uri("$norg2Url/enhet/navkontor/$geografiskOmraade?disk=$diskresjonskode")
                .header("Authorization", "Bearer ${tokenService.getEgressToken(norg2Scope, norg2Audience, pid, AppId.NORG2)}")
                .retrieve()
                .bodyToMono(NavEnhetResponse::class.java)
                .block()?.enhetId.toString()
        } catch (e: Exception) {
            if (e is WebClientResponseException) {
                when(e.statusCode) {
                    HttpStatus.NOT_FOUND -> {
                        throw ForbiddenException(AppId.NORG2.name, NORG2_API, "Ingen enhet funnet for geografisk område: $geografiskOmraade", null)
                    }
                    HttpStatus.FORBIDDEN -> {
                        throw ForbiddenException(AppId.NORG2.name, NORG2_API, "Ingen tilgang til Norg2 for geografisk område: $geografiskOmraade", null)
                    }
                    HttpStatus.INTERNAL_SERVER_ERROR -> {
                        throw ClientException(AppId.NORG2.name, NORG2_API, "Intern feil fra Norg2-api ved henting av enhet: $geografiskOmraade", null)
                    }
                    else -> {
                        throw ClientException(AppId.NORG2.name, NORG2_API, e.message, null)
                    }
                }
            }
            throw ClientException(AppId.PDL.name, NORG2_API, e.message, null)
        }
    }

    companion object {
        const val NORG2_API = "norg2-api"
    }
}