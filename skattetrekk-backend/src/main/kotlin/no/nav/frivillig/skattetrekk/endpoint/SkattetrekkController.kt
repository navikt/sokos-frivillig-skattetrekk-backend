package no.nav.frivillig.skattetrekk.endpoint

import no.nav.frivillig.skattetrekk.endpoint.api.*
import no.nav.frivillig.skattetrekk.security.SecurityContextUtil
import no.nav.frivillig.skattetrekk.service.BehandleTrekkService
import no.nav.frivillig.skattetrekk.service.HentSkattOgTrekkService
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/skattetrekk")
class SkattetrekkController(
    private val skattetrekkService: HentSkattOgTrekkService,
    private val behandleTrekkService: BehandleTrekkService
) {

    @GetMapping(produces = ["application/json"])
    fun getSkattetrekk(): ResponseEntity<FrivilligSkattetrekkInitResponse?> {
        val skatteTrekk =  skattetrekkService.hentSkattetrekk(SecurityContextUtil.getPidFromContext())
        return ResponseEntity<FrivilligSkattetrekkInitResponse?>(skatteTrekk, HttpStatus.OK)
    }

    @PostMapping(consumes = ["application/json"])
    fun opprettFrivilligSkattetrekk(@RequestBody request: OpprettRequest): OpprettResponse {
        val trekkvedtakId = behandleTrekkService.opprettTrekk(SecurityContextUtil.getPidFromContext(), request.value, request.satsType)
        return OpprettResponse(trekkvedtakId = trekkvedtakId)
    }

    @PutMapping(consumes = ["application/json"])
    fun updateFrivilligSkattetrekk(@RequestBody request: OppdaterRequest): OppdaterResponse {
        val trekkvedtakId = behandleTrekkService.oppdaterTrekk(
            SecurityContextUtil.getPidFromContext(),
            request.trekkVedtakId,
            request.value,
            request.satsType)

        return OppdaterResponse(trekkvedtakId = trekkvedtakId)
    }

    @DeleteMapping(consumes = ["application/json"])
    fun opphoerFrivilligSkattetrekk(@RequestBody request: OpphoerRequest) {
        behandleTrekkService.opphoerTrekk(SecurityContextUtil.getPidFromContext(), request.trekkVedtakId)
    }

    @ResponseStatus(value = SERVICE_UNAVAILABLE, reason = "Oppdragssystemet er nede eller utilgjengelig")
    @ExceptionHandler(OppdragUtilgjengeligException::class)
    fun oppdragErNede() = Unit

    @ResponseStatus(value = SERVICE_UNAVAILABLE, reason = "Teknisk feil fra Oppdragssystemet, prøv igjen senere")
    @ExceptionHandler(TekniskFeilFraOppdragException::class)
    fun tekniskFeilFraOppdrag() = Unit

    @ResponseStatus(value = SERVICE_UNAVAILABLE, reason = "Feil mot andre interne tjenester")
    @ExceptionHandler(ClientException::class)
    fun feilmotKlienter() = Unit

    @ResponseStatus(value = BAD_REQUEST, reason = "Fullmaktsforhold finnes ikke")
    @ExceptionHandler(NoFullmaktPresentException::class)
    fun fullmaktFinnesIkke() = Unit

    @ResponseStatus(value = UNAUTHORIZED, reason = "Autentisering mangler")
    @ExceptionHandler(UnauthorizedException::class)
    fun uautentisertBruker() = Unit

    @ResponseStatus(value = FORBIDDEN, reason = "Autorisasjon mangler")
    @ExceptionHandler(UnauthorizedException::class)
    fun uautorisertBruker() = Unit

    @ResponseStatus(value = BAD_REQUEST, reason = "Person ikke funnet")
    @ExceptionHandler(PersonNotFoundException::class)
    fun personFinnesIkke() = Unit

}