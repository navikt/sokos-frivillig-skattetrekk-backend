package no.nav.frivillig.skattetrekk.client.trekk

import no.nav.frivillig.skattetrekk.client.trekk.api.*
import no.nav.frivillig.skattetrekk.configuration.AppId
import no.nav.frivillig.skattetrekk.endpoint.ClientException
import no.nav.frivillig.skattetrekk.security.TokenService
import no.nav.frivillig.skattetrekk.service.TrekkTypeCode
import no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy.OppdaterAndreTrekkRequest
import no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy.OpphorAndreTrekkRequest
import no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy.OpprettAndreTrekkRequest

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class TrekkClient(
    @Value("\${trekk.endpoint.url}") private val trekkUrl: String,
    @Value("\${trekk.scope}") private val trekkScope: String,
    @Value("\${trekk.audience}") private val audience: String,
    private val tokenService: TokenService,
    private val webClient: WebClient,
) {

    fun finnTrekkListe(pid: String, trekkType: TrekkTypeCode): List<TrekkInfo> {

        val request = FinnTrekkListeRequest(
            debitorSok = DebitorSok(
                debitorOffnr = pid,
                filter = DebitorFilter(
                    trekktypeKode = trekkType.name
                )
            )
        )

        try {
            return webClient
                .post()
                .uri("$trekkUrl/api/nav-tjeneste-trekk/finnTrekkListe")
                .header("Authorization", "Bearer ${tokenService.getEgressToken(trekkScope, audience, pid, AppId.OPPDRAG_REST_PROXY)}")
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(FinnTrekkListeResponse::class.java)
                .block()
                ?.trekkInfoListe
                ?.toList()
                ?: emptyList()
        } catch (e: Exception) {
            if (e is WebClientResponseException) {
                when(e.statusCode) {
                    HttpStatus.BAD_REQUEST -> throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, e.message, null)
                    else -> throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, e.message, null)
                }
            }
            throw throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, "Failed to fetch trekkliste: ${e.message}", null)
        }
    }

    fun hentSkattOgTrekk(pid: String, trekkVedtakId: Long): HentSkattOgTrekkResponse? {

        val request = HentSkattOgTrekkRequest(trekkvedtakId = trekkVedtakId)

        try {
            return webClient
                .post()
                .uri("$trekkUrl/api/nav-tjeneste-trekk/hentSkattOgTrekk")
                .header("Authorization", "Bearer ${tokenService.getEgressToken(trekkScope, audience, pid, AppId.OPPDRAG_REST_PROXY)}")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(HentSkattOgTrekkResponse::class.java)
                .block()
        } catch (e: Exception) {
            if (e is WebClientResponseException) {
                when(e.statusCode) {
                    HttpStatus.BAD_REQUEST -> throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, e.message, null)
                    else -> throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, e.message, null)
                }
            }
            throw throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, "Failed to fetch trekkliste: ${e.message}", null)
        }
    }

    fun opprettAndreTrekk(pid: String, request: OpprettAndreTrekkRequest): OpprettAndreTrekkResponse? =
        try {
            webClient
                .post()
                .uri("$trekkUrl/api/nav-tjeneste-behandleTrekk/opprettAndreTrekk")
                .header("Authorization", "Bearer ${tokenService.getEgressToken(trekkScope, audience, pid, AppId.OPPDRAG_REST_PROXY)}")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpprettAndreTrekkResponse::class.java)
                .block()
        } catch(e: Exception) {
            throw RuntimeException("Failed to fetch skattetrekk", e)
        }

    fun opphorAndreTrekk(pid: String, request: OpphorAndreTrekkRequest) {
        try {
            webClient
                .post()
                .uri("$trekkUrl/api/nav-tjeneste-behandleTrekk/opphorAndreTrekk")
                .header("Authorization", "Bearer ${tokenService.getEgressToken(trekkScope, audience, pid, AppId.OPPDRAG_REST_PROXY)}")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block()
        } catch(e: Exception) {
            throw RuntimeException("Failed to fetch skattetrekk", e)
        }
    }

    companion object {
        const val TREKK_API = "trekk-api"
    }
}