package no.nav.sokos.frivillig.skattetrekk.backend.client.trekk

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.DebitorFilter
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.DebitorSok
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.FinnTrekkListeRequest
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.FinnTrekkListeResponse
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.HentSkattOgTrekkRequest
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.HentSkattOgTrekkResponse
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.OppdaterAndreTrekkRequest
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.OpphorAndreTrekkRequest
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.OpprettAndreTrekkRequest
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.OpprettAndreTrekkResponse
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.TrekkInfo
import no.nav.sokos.frivillig.skattetrekk.backend.configuration.AppId
import no.nav.sokos.frivillig.skattetrekk.backend.configuration.TrekkConfig
import no.nav.sokos.frivillig.skattetrekk.backend.endpoint.ClientException
import no.nav.sokos.frivillig.skattetrekk.backend.endpoint.OppdragUtilgjengeligException
import no.nav.sokos.frivillig.skattetrekk.backend.endpoint.TekniskFeilFraOppdragException
import no.nav.sokos.frivillig.skattetrekk.backend.security.TokenService
import no.nav.sokos.frivillig.skattetrekk.backend.service.TrekkTypeCode

private val logger = KotlinLogging.logger {}

@Component
class TrekkClient(
    private val trekkConfig: TrekkConfig,
    private val tokenService: TokenService,
    private val webClient: WebClient,
) {
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
                .uri("${trekkConfig.trekkUrl}/api/nav-tjeneste-trekk/finnTrekkListe")
                .header("Authorization", "Bearer ${tokenService.getEgressToken(trekkConfig.trekkScope, trekkConfig.audience, pid, AppId.OPPDRAG_REST_PROXY)}")
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(FinnTrekkListeResponse::class.java)
                .block()
                ?.trekkInfoListe
                ?.toList()
                ?: emptyList()
        } catch (e: Exception) {
            logger.error("Failed to fetch trekkliste: ${e.message}", e)
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
                .uri("${trekkConfig.trekkUrl}/api/nav-tjeneste-trekk/hentSkattOgTrekk")
                .header("Authorization", "Bearer ${tokenService.getEgressToken(trekkConfig.trekkScope, trekkConfig.audience, pid, AppId.OPPDRAG_REST_PROXY)}")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(HentSkattOgTrekkResponse::class.java)
                .block()
        } catch (e: Exception) {
            logger.error("Failed to hentSkattOgTrekk: ${e.message}", e)
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
                .uri("${trekkConfig.trekkUrl}/api/nav-tjeneste-behandleTrekk/opprettAndreTrekk")
                .header("Authorization", "Bearer ${tokenService.getEgressToken(trekkConfig.trekkScope, trekkConfig.audience, pid, AppId.OPPDRAG_REST_PROXY)}")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpprettAndreTrekkResponse::class.java)
                .block()
        } catch (e: Exception) {
            logger.error("Failed to opprettAndreTrekk: ${e.message}", e)
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
                .uri("${trekkConfig.trekkUrl}/api/nav-tjeneste-behandleTrekk/oppdaterAndreTrekk")
                .header(
                    "Authorization",
                    "Bearer ${tokenService.getEgressToken(trekkConfig.trekkScope, trekkConfig.audience, pid, AppId.OPPDRAG_REST_PROXY)}",
                ).bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block()
        } catch (e: Exception) {
            logger.error("Failed to oppdaterAndreTrekk: ${e.message}", e)
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
                null,
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
                .uri("${trekkConfig.trekkUrl}/api/nav-tjeneste-behandleTrekk/opphorAndreTrekk")
                .header("Authorization", "Bearer ${tokenService.getEgressToken(trekkConfig.trekkScope, trekkConfig.audience, pid, AppId.OPPDRAG_REST_PROXY)}")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block()
        } catch (e: Exception) {
            logger.error("Failed to opphorAndreTrekk: ${e.message}", e)
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
