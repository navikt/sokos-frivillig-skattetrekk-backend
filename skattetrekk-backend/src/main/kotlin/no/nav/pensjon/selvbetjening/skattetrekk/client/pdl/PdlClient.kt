package no.nav.pensjon.selvbetjening.skattetrekk.client.pdl

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.pensjon.selvbetjening.skattetrekk.client.pdl.api.*
import no.nav.pensjon.selvbetjening.skattetrekk.client.util.CallIdUtil
import no.nav.pensjon.selvbetjening.skattetrekk.client.util.getCallIdFromMdc
import no.nav.pensjon.selvbetjening.skattetrekk.configuration.AppId
import no.nav.pensjon.selvbetjening.skattetrekk.configuration.CallIdUtil.Companion.NAV_CALL_ID_NAME
import no.nav.pensjon.selvbetjening.skattetrekk.security.TokenService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class PdlClient(
    private val webClient: WebClient,
    @Value("\${pdl.endpoint.url}") private val url: String,
    @Value("\${pdl.scope}") private val scope: String,
    @Value("\${pdl.audience}") private val audience: String,
    private val tokenService: TokenService
) {

    fun hentGeografiskTilknytningOgAdresseBeskyttelseQuery(pid: String): HentPdlGeografiskTilknytningOgAdressebeskyttelseResponse? {
        return tokenService.getEgressToken(scope, audience, pid, AppId.PDL)
            ?.let { webClient.post()
                .uri(url)
                .header("Authorization", "Bearer $it")
                .header(NAV_CALL_ID_NAME, CallIdUtil.getCallIdFromMdc())
                .header(PDL_TEMA_ATTRIBUTE_NAME, PDL_TEMA_VALUE)
                .header(PDL_BEHANDLINGSNUMMER_KEY, PDL_BEHANDLINGSNUMMER_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(hentBasertPaaQuery(pid)), PdlPersonQuery::class.java)
                .retrieve()
                .bodyToMono(HentPdlGeografiskTilknytningOgAdressebeskyttelseResponse::class.java) }
                ?.block()
    }

    companion object {
        const val PDL_TEMA_VALUE = "PEN"
        const val PDL_TEMA_ATTRIBUTE_NAME = "Tema"
        const val PDL_BEHANDLINGSNUMMER_VALUE = "B280"
        const val PDL_BEHANDLINGSNUMMER_KEY = "Behandlingsnummer"
    }
}

fun hentBasertPaaQuery(pid: String) = PdlPersonQuery(
    PdlPersonQuery::class.java.getResource("/pdl/GeografiskTilknytningOgAdressebeskyttelseQuery.graphql")
        ?.readText()?.replace("[ \n\r]", "")
        ?: throw IllegalArgumentException("Unable to locate graphQl file"),
    PdlPersonVariables(pid)
)

data class PdlPersonQuery(
    val query: String,
    val variables: PdlPersonVariables
)

data class PdlPersonVariables(
    val ident: String
)