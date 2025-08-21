package no.nav.frivillig.skattetrekk.client.trekk

import no.nav.frivillig.skattetrekk.client.trekk.api.DebitorFilter
import no.nav.frivillig.skattetrekk.client.trekk.api.DebitorSok
import no.nav.frivillig.skattetrekk.client.trekk.api.FinnTrekkListeRequest
import no.nav.frivillig.skattetrekk.client.trekk.api.FinnTrekkListeResponse
import no.nav.frivillig.skattetrekk.client.trekk.api.HentSkattOgTrekkRequest
import no.nav.frivillig.skattetrekk.client.trekk.api.HentSkattOgTrekkResponse
import no.nav.frivillig.skattetrekk.client.trekk.api.OpprettAndreTrekkResponse
import no.nav.frivillig.skattetrekk.client.trekk.api.TrekkInfo
import no.nav.frivillig.skattetrekk.configuration.AppId
import no.nav.frivillig.skattetrekk.endpoint.ClientException
import no.nav.frivillig.skattetrekk.endpoint.OppdragUtilgjengeligException
import no.nav.frivillig.skattetrekk.endpoint.TekniskFeilFraOppdragException
import no.nav.frivillig.skattetrekk.security.TokenService
import no.nav.frivillig.skattetrekk.service.TrekkTypeCode
import no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy.OppdaterAndreTrekkRequest
import no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy.OpphorAndreTrekkRequest
import no.nav.pensjon.pselv.consumer.behandletrekk.oppdragrestproxy.OpprettAndreTrekkRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
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
    private val log = LoggerFactory.getLogger(TrekkClient::class.java)

    fun finnTrekkListe(
        pid: String,
        trekkType: TrekkTypeCode,
    ): List<TrekkInfo> {
        val request =
            FinnTrekkListeRequest(
                debitorSok =
                    DebitorSok(
                        debitorOffnr = pid,
                        filter =
                            DebitorFilter(
                                trekktypeKode = trekkType.name,
                            ),
                    ),
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
            log.error("Failed to fetch trekkliste: ${e.message}", e)
            if (e is WebClientResponseException) {
                when (e.message) {
                    "Oppdragssystemet er nede eller utilgjengelig" -> throw OppdragUtilgjengeligException()
                    "Teknisk feil fra Oppdragssystemet, prøv igjen senere" -> throw TekniskFeilFraOppdragException()
                    else -> throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, e.message, null)
                }
            }
            throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, "Failed to fetch trekkliste: ${e.message}", null)
        }
    }

    fun hentSkattOgTrekk(
        pid: String,
        trekkVedtakId: Long,
    ): HentSkattOgTrekkResponse? {
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
            log.error("Failed to hentSkattOgTrekk: ${e.message}", e)
            if (e is WebClientResponseException) {
                when (e.message) {
                    "Oppdragssystemet er nede eller utilgjengelig" -> throw OppdragUtilgjengeligException()
                    "Teknisk feil fra Oppdragssystemet, prøv igjen senere" -> throw TekniskFeilFraOppdragException()
                    else -> throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, e.message, null)
                }
            }
            throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, "Failed to fetch trekkliste: ${e.message}", null)
        }
    }

    fun opprettAndreTrekk(
        pid: String,
        request: OpprettAndreTrekkRequest,
    ): OpprettAndreTrekkResponse? =
        try {
            webClient
                .post()
                .uri("$trekkUrl/api/nav-tjeneste-behandleTrekk/opprettAndreTrekk")
                .header("Authorization", "Bearer ${tokenService.getEgressToken(trekkScope, audience, pid, AppId.OPPDRAG_REST_PROXY)}")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpprettAndreTrekkResponse::class.java)
                .block()
        } catch (e: Exception) {
            log.error("Failed to opprettAndreTrekk: ${e.message}", e)
            if (e is WebClientResponseException) {
                when (e.message) {
                    "Oppdragssystemet er nede eller utilgjengelig" -> throw OppdragUtilgjengeligException()
                    "Teknisk feil fra Oppdragssystemet, prøv igjen senere" -> throw TekniskFeilFraOppdragException()
                    else -> throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, e.message, null)
                }
            }
            throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, "Failed to opprett andre trekk: ${e.message}", null)
        }

    fun oppdaterAndreTrekk(
        pid: String,
        request: OppdaterAndreTrekkRequest,
    ) {
        try {
            webClient
                .post()
                .uri("$trekkUrl/api/nav-tjeneste-behandleTrekk/oppdaterAndreTrekk")
                .header(
                    "Authorization",
                    "Bearer ${tokenService.getEgressToken(trekkScope, audience, pid, AppId.OPPDRAG_REST_PROXY)}"
                )
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block()
        } catch (e: Exception) {
            log.error("Failed to oppdaterAndreTrekk: ${e.message}", e)
            if (e is WebClientResponseException) {
                when (e.message) {
                    "Oppdragssystemet er nede eller utilgjengelig" -> throw OppdragUtilgjengeligException()
                    "Teknisk feil fra Oppdragssystemet, prøv igjen senere" -> throw TekniskFeilFraOppdragException()
                    else -> throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, e.message, null)
                }
            }
            throw ClientException(
                AppId.OPPDRAG_REST_PROXY.name,
                TREKK_API,
                "Failed to oppdater andre trekk: ${e.message}",
                null
            )
        }
    }

    fun opphorAndreTrekk(
        pid: String,
        request: OpphorAndreTrekkRequest,
    ) {
        try {
            webClient
                .post()
                .uri("$trekkUrl/api/nav-tjeneste-behandleTrekk/opphorAndreTrekk")
                .header("Authorization", "Bearer ${tokenService.getEgressToken(trekkScope, audience, pid, AppId.OPPDRAG_REST_PROXY)}")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block()
        } catch (e: Exception) {
            log.error("Failed to opphorAndreTrekk: ${e.message}", e)
            if (e is WebClientResponseException) {
                when (e.message) {
                    "Oppdragssystemet er nede eller utilgjengelig" -> throw OppdragUtilgjengeligException()
                    "Teknisk feil fra Oppdragssystemet, prøv igjen senere" -> throw TekniskFeilFraOppdragException()
                    else -> throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, e.message, null)
                }
            }
            throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, "Failed to opphor andre trekk: ${e.message}", null)
        }
    }

    companion object {
        const val TREKK_API = "trekk-api"
    }
}
