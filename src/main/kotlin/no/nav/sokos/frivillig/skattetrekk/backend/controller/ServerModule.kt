package no.nav.sokos.frivillig.skattetrekk.backend.controller

import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.serialization.jackson3.jackson
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import java.security.interfaces.RSAPublicKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.sokos.frivillig.skattetrekk.backend.controller.models.BehandleRequest
import no.nav.sokos.frivillig.skattetrekk.backend.security.RequestContext
import no.nav.sokos.frivillig.skattetrekk.backend.service.BehandleTrekkService
import no.nav.sokos.frivillig.skattetrekk.backend.service.HentSkattOgTrekkService
import no.nav.sokos.frivillig.skattetrekk.backend.service.Validering

fun Application.skattetrekkModule(
    skattetrekkService: HentSkattOgTrekkService,
    behandleTrekkService: BehandleTrekkService,
    issuer: String,
    jwkProvider: JwkProvider,
    meterRegistry: PrometheusMeterRegistry,
) {
    install(ContentNegotiation) { jackson() }
    install(MicrometerMetrics) { registry = meterRegistry }
    install(Authentication) {
        jwt("token-x") {
            verifier { header ->
                try {
                    val token = (header as? HttpAuthHeader.Single)?.blob ?: return@verifier null
                    val decoded = JWT.decode(token)
                    if (decoded.algorithm != "RS256") return@verifier null
                    val publicKey = withContext(Dispatchers.IO) { jwkProvider.get(decoded.keyId).publicKey as RSAPublicKey }
                    JWT.require(Algorithm.RSA256(publicKey, null)).withIssuer(issuer).acceptLeeway(60).build()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    null
                }
            }
            validate { credential ->
                if (!credential.payload.subject.isNullOrBlank()) JWTPrincipal(credential.payload) else null
            }
        }
    }
    install(StatusPages) {
        exception<PersonNotFoundException> { call, _ -> call.respond(HttpStatusCode.BadRequest) }
        exception<ClientException> { call, _ -> call.respond(HttpStatusCode.ServiceUnavailable) }
        exception<TekniskFeilFraOppdragException> { call, _ -> call.respond(HttpStatusCode.ServiceUnavailable) }
        exception<OppdragUtilgjengeligException> { call, _ -> call.respond(HttpStatusCode.ServiceUnavailable) }
        exception<UnauthorizedException> { call, _ -> call.respond(HttpStatusCode.Forbidden) }
        exception<ForbiddenException> { call, _ -> call.respond(HttpStatusCode.Forbidden) }
    }
    routing {
        get("/actuator/health") { call.respond(mapOf("status" to "UP")) }
        get("/actuator/health/liveness") { call.respond(mapOf("status" to "UP")) }
        get("/actuator/health/readiness") { call.respond(mapOf("status" to "UP")) }
        get("/actuator/prometheus") { call.respondText(meterRegistry.scrape(), io.ktor.http.ContentType.parse("text/plain; version=0.0.4")) }
        authenticate("token-x") {
            route("/api/skattetrekk") {
                get {
                    val principal = call.principal<JWTPrincipal>()!!
                    val context = RequestContext(principal.payload.subject, principal.payload.issuer, principal.payload.getClaim("acr").asString(), call.request.headers[HttpHeaders.Authorization]!!.substringAfter(' '))
                    val result = skattetrekkService.hentSkattetrekk(context)
                    if (result == null) call.respond(HttpStatusCode.OK) else call.respond(result)
                }
                post {
                    val principal = call.principal<JWTPrincipal>()!!
                    val context = RequestContext(principal.payload.subject, principal.payload.issuer, principal.payload.getClaim("acr").asString(), call.request.headers[HttpHeaders.Authorization]!!.substringAfter(' '))
                    val request = call.receive<BehandleRequest>()
                    if (Validering.valider(request.value, request.satsType).isEmpty()) {
                        behandleTrekkService.behandleTrekk(context, request.value, request.satsType)
                    }
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}
