package no.nav.sokos.frivillig.skattetrekk.backend.client.trekk

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

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
import no.nav.sokos.frivillig.skattetrekk.backend.config.AppId
import no.nav.sokos.frivillig.skattetrekk.backend.controller.ClientException
import no.nav.sokos.frivillig.skattetrekk.backend.controller.OppdragUtilgjengeligException
import no.nav.sokos.frivillig.skattetrekk.backend.controller.TekniskFeilFraOppdragException
import no.nav.sokos.frivillig.skattetrekk.backend.security.RequestContext
import no.nav.sokos.frivillig.skattetrekk.backend.security.TokenService
import no.nav.sokos.frivillig.skattetrekk.backend.service.TrekkTypeCode

private val logger = KotlinLogging.logger {}

class TrekkClient(
    private val trekkUrl: String,
    private val trekkScope: String,
    private val audience: String,
    private val tokenService: TokenService,
    private val httpClient: HttpClient,
) {
    fun finnTrekkListe(
        context: RequestContext,
        trekkType: TrekkTypeCode,
    ): List<no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.TrekkInfo> {
        val pid = context.pid
        val request =
            FinnTrekkListeRequest(
                debitorSok =
                    DebitorSok(
                        debitorOffnr = pid,
                        filter =
                            DebitorFilter(
                                trekktypeKode = trekkType.name,
                                visAvsluttede = false,
                            ),
                    ),
            )

        try {
            return runBlocking {
                post(context, "/api/nav-tjeneste-trekk/finnTrekkListe", request).bodyOrNull<FinnTrekkListeResponse>()
            }
                ?.trekkInfoListe
                ?.toList()
                ?: emptyList()
        } catch (e: Exception) {
            logger.error("Failed to fetch trekkliste: ${e.message}", e)
            if (e is ResponseException) {
                when (e.message) {
                    "Oppdragssystemet er nede eller utilgjengelig" -> throw OppdragUtilgjengeligException()
                    "Teknisk feil fra Oppdragssystemet, prøv igjen senere" -> throw TekniskFeilFraOppdragException()
                    else -> throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, e.webClientMessage(), null)
                }
            }
            throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, "Failed to fetch trekkliste: ${e.message}", null)
        }
    }

    fun hentSkattOgTrekk(
        context: RequestContext,
        trekkVedtakId: Long,
    ): HentSkattOgTrekkResponse? {
        val request = HentSkattOgTrekkRequest(trekkvedtakId = trekkVedtakId)

        try {
            return runBlocking {
                post(context, "/api/nav-tjeneste-trekk/hentSkattOgTrekk", request).bodyOrNull<HentSkattOgTrekkResponse>()
            }
        } catch (e: Exception) {
            logger.error("Failed to hentSkattOgTrekk: ${e.message}", e)
            if (e is ResponseException) {
                when (e.message) {
                    "Oppdragssystemet er nede eller utilgjengelig" -> throw OppdragUtilgjengeligException()
                    "Teknisk feil fra Oppdragssystemet, prøv igjen senere" -> throw TekniskFeilFraOppdragException()
                    else -> throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, e.webClientMessage(), null)
                }
            }
            throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, "Failed to fetch trekkliste: ${e.message}", null)
        }
    }

    fun opprettAndreTrekk(
        context: RequestContext,
        request: OpprettAndreTrekkRequest,
    ): OpprettAndreTrekkResponse? =
        try {
            runBlocking {
                post(context, "/api/nav-tjeneste-behandleTrekk/opprettAndreTrekk", request).bodyOrNull<OpprettAndreTrekkResponse>()
            }
        } catch (e: Exception) {
            logger.error("Failed to opprettAndreTrekk: ${e.message}", e)
            if (e is ResponseException) {
                when (e.message) {
                    "Oppdragssystemet er nede eller utilgjengelig" -> throw OppdragUtilgjengeligException()
                    "Teknisk feil fra Oppdragssystemet, prøv igjen senere" -> throw TekniskFeilFraOppdragException()
                    else -> throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, e.webClientMessage(), null)
                }
            }
            throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, "Failed to opprett andre trekk: ${e.message}", null)
        }

    fun oppdaterAndreTrekk(
        context: RequestContext,
        request: OppdaterAndreTrekkRequest,
    ) {
        try {
            runBlocking {
                post(context, "/api/nav-tjeneste-behandleTrekk/oppdaterAndreTrekk", request).bodyAsText()
            }
        } catch (e: Exception) {
            logger.error("Failed to oppdaterAndreTrekk: ${e.message}", e)
            if (e is ResponseException) {
                when (e.message) {
                    "Oppdragssystemet er nede eller utilgjengelig" -> throw OppdragUtilgjengeligException()
                    "Teknisk feil fra Oppdragssystemet, prøv igjen senere" -> throw TekniskFeilFraOppdragException()
                    else -> throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, e.webClientMessage(), null)
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
        context: RequestContext,
        request: OpphorAndreTrekkRequest,
    ) {
        try {
            runBlocking {
                post(context, "/api/nav-tjeneste-behandleTrekk/opphorAndreTrekk", request).bodyAsText()
            }
        } catch (e: Exception) {
            logger.error("Failed to opphorAndreTrekk: ${e.message}", e)
            if (e is ResponseException) {
                when (e.message) {
                    "Oppdragssystemet er nede eller utilgjengelig" -> throw OppdragUtilgjengeligException()
                    "Teknisk feil fra Oppdragssystemet, prøv igjen senere" -> throw TekniskFeilFraOppdragException()
                    else -> throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, e.webClientMessage(), null)
                }
            }
            throw ClientException(AppId.OPPDRAG_REST_PROXY.name, TREKK_API, "Failed to opphor andre trekk: ${e.message}", null)
        }
    }

    private suspend inline fun <reified T : Any> post(context: RequestContext, path: String, request: T): HttpResponse =
        httpClient.post("$trekkUrl$path") {
            headers.append(HttpHeaders.Authorization, "Bearer ${tokenService.getEgressToken(trekkScope, audience, context, AppId.OPPDRAG_REST_PROXY)}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    private suspend inline fun <reified T : Any> HttpResponse.bodyOrNull(): T? =
        if (bodyAsText().let { it.isEmpty() || it.trim() == "null" }) null else body<T>()

    private fun ResponseException.webClientMessage(): String =
        "${response.status.value} ${response.status.description} from ${response.call.request.method.value} ${response.call.request.url}"

    companion object {
        const val TREKK_API = "trekk-api"
    }
}
