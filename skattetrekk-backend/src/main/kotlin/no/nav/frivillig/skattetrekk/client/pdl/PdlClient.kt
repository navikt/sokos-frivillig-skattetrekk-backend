package no.nav.frivillig.skattetrekk.client.pdl

import no.nav.frivillig.skattetrekk.client.CallIdUtil
import no.nav.frivillig.skattetrekk.client.CallIdUtil.Companion.NAV_CALL_ID_NAME
import no.nav.frivillig.skattetrekk.client.getCallIdFromMdc
import no.nav.frivillig.skattetrekk.client.pdl.api.*
import no.nav.frivillig.skattetrekk.configuration.AppId
import no.nav.frivillig.skattetrekk.endpoint.ClientException
import no.nav.frivillig.skattetrekk.endpoint.ForbiddenException
import no.nav.frivillig.skattetrekk.endpoint.PersonNotFoundException
import no.nav.frivillig.skattetrekk.security.TokenService
import org.slf4j.LoggerFactory
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

    private val logger = LoggerFactory.getLogger(PdlClient::class.java)

    fun hentGeografiskTilknytningOgAdresseBeskyttelseQuery(pid: String): HentPdlGeografiskTilknytningOgAdressebeskyttelseResponse? {
         val response = tokenService.getEgressToken(scope, audience, pid, AppId.PDL)
            ?.let { webClient.post()
                .uri(url)
                .header("Authorization", "Bearer $it")
                .header(NAV_CALL_ID_NAME, CallIdUtil.getCallIdFromMdc())
                .header(PDL_BEHANDLINGSNUMMER_KEY, PDL_BEHANDLINGSNUMMER_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(hentBasertPaaQuery(pid)), PdlPersonQuery::class.java)
                .retrieve()
                .bodyToMono(HentPdlGeografiskTilknytningOgAdressebeskyttelseResponse::class.java) }
                ?.block()

        return if (response != null && response.errors.isNullOrEmpty()) {
            response
        } else {
            response?.errors.takeIf { !it.isNullOrEmpty() }?.let { errors ->
                val error = errors.first()
                logger.error("Kallet feilet mot PDL: kode ${error.extensions?.code}")
                when (error.extensions?.code) {
                    PdlErrorCodes.UNAUTHENTICATED -> throw ForbiddenException(
                        AppId.PDL.name,
                        PDL_API,
                        error.message,
                        null
                    )

                    PdlErrorCodes.UNAUTHORIZED -> throw ForbiddenException(
                        AppId.PDL.name,
                        PDL_API,
                        error.message,
                        null
                    )

                    PdlErrorCodes.NOT_FOUND -> throw PersonNotFoundException(AppId.PDL.name, PDL_API, error.message, null)
                    PdlErrorCodes.BAD_REQUEST -> throw ClientException(AppId.PDL.name, PDL_API, error.message, null)
                    PdlErrorCodes.SERVER_ERROR -> throw ClientException(AppId.PDL.name, PDL_API, error.message, null)
                    else -> throw ClientException(AppId.PDL.name, PDL_API, error.message, null)
                }
            }
            throw ClientException(AppId.PDL.name, PDL_API, "Failed calling PDL", null)
        }
    }

    companion object {
        const val PDL_BEHANDLINGSNUMMER_VALUE = "B154"
        const val PDL_BEHANDLINGSNUMMER_KEY = "Behandlingsnummer"
        const val PDL_API = "pdl-api"
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