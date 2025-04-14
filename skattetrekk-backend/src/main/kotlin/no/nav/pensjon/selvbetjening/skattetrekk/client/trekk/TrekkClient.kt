package no.nav.pensjon.selvbetjening.skattetrekk.client.trekk

import no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy.OppdaterAndreTrekkRequest
import no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy.OpphorAndreTrekkRequest
import no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy.OpprettAndreTrekkRequest
import no.nav.pensjon.selvbetjening.skattetrekk.client.trekk.api.*
import no.nav.pensjon.selvbetjening.skattetrekk.configuration.AppId
import no.nav.pensjon.selvbetjening.skattetrekk.security.TokenService
import no.nav.pensjon.selvbetjening.skattetrekk.service.TrekkTypeCode

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

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
            throw RuntimeException("Failed to fetch trekkliste", e)
        }
    }

    fun hentSkattOgTrekk(pid: String, trekkVedtakId: Long): HentSkattOgTrekkResponse {

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
                ?: throw RuntimeException("Failed to fetch skattetrekk")
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch skattetrekk", e)
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
                ?: throw RuntimeException("Failed to fetch skattetrekk")
        } catch(e: Exception) {
            throw RuntimeException("Failed to fetch skattetrekk", e)
        }

    fun oppdaterAndreTrekk(pid: String, request: OppdaterAndreTrekkRequest) =
        try {
            webClient
                .post()
                .uri("$trekkUrl/api/nav-tjeneste-behandleTrekk/oppdaterAndreTrekk")
                .header("Authorization", "Bearer ${tokenService.getEgressToken(trekkScope, audience, pid, AppId.OPPDRAG_REST_PROXY)}")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block()
                ?: throw RuntimeException("Failed to fetch skattetrekk")
        } catch(e: Exception) {
            throw RuntimeException("Failed to fetch skattetrekk", e)
        }

    fun opphorAndreTrekk(pid: String, request: OpphorAndreTrekkRequest) =
        try {
            webClient
                .post()
                .uri("$trekkUrl/api/nav-tjeneste-behandleTrekk/opphorAndreTrekk")
                .header("Authorization", "Bearer ${tokenService.getEgressToken(trekkScope, audience, pid, AppId.OPPDRAG_REST_PROXY)}")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block()
                ?: throw RuntimeException("Failed to fetch skattetrekk")
        } catch(e: Exception) {
            throw RuntimeException("Failed to fetch skattetrekk", e)
        }
}