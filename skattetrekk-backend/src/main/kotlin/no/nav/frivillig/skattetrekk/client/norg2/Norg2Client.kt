package no.nav.frivillig.skattetrekk.client.norg2

import no.nav.frivillig.skattetrekk.client.norg2.api.NavEnhetResponse
import no.nav.frivillig.skattetrekk.configuration.AppId
import no.nav.frivillig.skattetrekk.security.TokenService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

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
            throw RuntimeException("Failed to fetch Nav enhet-id", e)
        }
    }
}