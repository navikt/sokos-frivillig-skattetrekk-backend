package no.nav.sokos.frivillig.skattetrekk.backend.endpoint

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

import no.nav.frivillig.skattetrekk.endpoint.api.BehandleRequest
import no.nav.frivillig.skattetrekk.endpoint.api.FrivilligSkattetrekkInitResponse
import no.nav.frivillig.skattetrekk.security.SecurityContextUtil
import no.nav.frivillig.skattetrekk.service.BehandleTrekkService
import no.nav.frivillig.skattetrekk.service.HentSkattOgTrekkService
import no.nav.frivillig.skattetrekk.service.Validering

@RestController
@RequestMapping("api/skattetrekk")
class SkattetrekkController(
    private val skattetrekkService: HentSkattOgTrekkService,
    private val behandleTrekkService: BehandleTrekkService,
) {
    @GetMapping(produces = ["application/json"])
    fun getSkattetrekk(): FrivilligSkattetrekkInitResponse? = skattetrekkService.hentSkattetrekk(SecurityContextUtil.getPidFromContext())

    @PostMapping
    fun behandleFrivilligSkattetrekk(
        @RequestBody request: BehandleRequest,
    ) {
        val valideringsListe = Validering.valider(request.value, request.satsType)
        if (valideringsListe.isEmpty()) {
            behandleTrekkService.behandleTrekk(
                SecurityContextUtil.getPidFromContext(),
                request.value,
                request.satsType,
            )
        }
    }

    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE, reason = "Teknisk feil fra Oppdragssystemet, prøv igjen senere")
    @ExceptionHandler(TekniskFeilFraOppdragException::class)
    fun tekniskFeilFraOppdrag() = Unit

    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE, reason = "Oppdragsystemet er nede eller utilgjengelig")
    @ExceptionHandler(OppdragUtilgjengeligException::class)
    fun oppdragErNede() = Unit

    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE, reason = "Feil mot andre interne tjenester")
    @ExceptionHandler(ClientException::class)
    fun feilmotKlienter() = Unit

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Person finnes ikke i PDL")
    @ExceptionHandler(PersonNotFoundException::class)
    fun personFinnesIkke() = Unit

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Autorisasjon mangler")
    @ExceptionHandler(UnauthorizedException::class)
    fun uautorisertBruker() = Unit

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Nekter tilgang")
    @ExceptionHandler(ForbiddenException::class)
    fun tilgangsnekt() = Unit
}
