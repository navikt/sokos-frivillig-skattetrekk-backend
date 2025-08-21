package no.nav.frivillig.skattetrekk.utbetaling

import java.time.LocalDate

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.reactive.function.client.WebClient

import no.nav.frivillig.skattetrekk.configuration.AppId
import no.nav.frivillig.skattetrekk.security.TokenService
import no.nav.frivillig.skattetrekk.utbetaling.model.UtbetalDataApiFeil
import no.nav.frivillig.skattetrekk.utbetaling.model.UtbetalDataIkkeFunnet
import no.nav.frivillig.skattetrekk.utbetaling.model.UtbetalDataServerFeil
import no.nav.frivillig.skattetrekk.utbetaling.model.Utbetaling

private const val MND_FOM_DATO_FOER = 3

@Component
class UtbetalingClient(
    @Value("\${trekk.endpoint.url}") private val utbetalingEndpoint: String,
    @Value("\${trekk.scope}") private val trekkScope: String,
    @Value("\${trekk.audience}") private val audience: String,
    private val tokenService: TokenService,
    val webClient: WebClient,
) {
    fun fetchUtbetalinger(pid: String): List<Utbetaling> {
        val utbetalRequest =
            UtbetalRequest(
                ident = pid,
                rolle = Rolle.UTBETALT_TIL.name,
                periode =
                    Periode(
                        fom = LocalDate.now().minusMonths(MND_FOM_DATO_FOER.toLong()),
                        tom = LocalDate.now(),
                    ),
                periodetype = Periodetype.UTBETALINGSPERIODE.name,
            )
        val payload: HttpEntity<UtbetalRequest> = HttpEntity(utbetalRequest)
        val response: List<Utbetaling>

        try {
            response = webClient
                .post()
                .uri("$utbetalingEndpoint/utbetaldata/api/v2/hent-utbetalingsinformasjon/intern")
                .header("Authorization", "Bearer ${tokenService.getEgressToken(trekkScope, audience, pid, AppId.OPPDRAG_REST_PROXY)}")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<List<Utbetaling>>() {})
                .block()
                ?: throw UtbetalDataIkkeFunnet("Kunne ikke finne utbetalingsdata for pid = $pid")
        } catch (e: HttpClientErrorException) {
            throw UtbetalDataApiFeil("UtbetalData forespørsel feilet med meldingen: ${e.message}")
        } catch (e: HttpServerErrorException) {
            throw UtbetalDataServerFeil("Uhåndtert feil hos utbetaldata")
        }
        return response
    }
}

data class UtbetalRequest(
    val ident: String,
    val rolle: String,
    val periode: Periode,
    val periodetype: String,
)

data class Periode(
    val fom: LocalDate,
    val tom: LocalDate,
)

enum class Rolle {
    UTBETALT_TIL,
}

enum class Periodetype {
    UTBETALINGSPERIODE,
}
