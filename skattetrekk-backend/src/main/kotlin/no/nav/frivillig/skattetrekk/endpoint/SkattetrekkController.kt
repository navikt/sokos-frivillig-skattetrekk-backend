package no.nav.frivillig.skattetrekk.endpoint

import no.nav.frivillig.skattetrekk.endpoint.api.*
import no.nav.frivillig.skattetrekk.security.SecurityContextUtil
import no.nav.frivillig.skattetrekk.service.BehandleTrekkService
import no.nav.frivillig.skattetrekk.service.HentSkattOgTrekkService
import org.springframework.http.HttpStatus.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/skattetrekk")
class SkattetrekkController(
    private val skattetrekkService: HentSkattOgTrekkService,
    private val behandleTrekkService: BehandleTrekkService
) {

    @GetMapping(produces = ["application/json"])
    fun getSkattetrekk(): FrivilligSkattetrekkInitResponse? = skattetrekkService.hentSkattetrekk(SecurityContextUtil.getPidFromContext())

    @PostMapping
    fun behandleFrivilligSkattetrekk(@RequestBody request: BehandleRequest): FrivilligSkattetrekkInitResponse? {
        behandleTrekkService.behandleTrekk(
            SecurityContextUtil.getPidFromContext(),
            request.value,
            request.satsType
        )
        return skattetrekkService.hentSkattetrekk(SecurityContextUtil.getPidFromContext())
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

    @ResponseStatus(value = BAD_REQUEST, reason = "Person finnes ikke i PDL")
    @ExceptionHandler(PersonNotFoundException::class)
    fun personFinnesIkke() = Unit

    @ResponseStatus(value = FORBIDDEN, reason = "Autorisasjon mangler")
    @ExceptionHandler(UnauthorizedException::class)
    fun uautorisertBruker() = Unit

    @ResponseStatus(value = FORBIDDEN, reason = "Nekter tilgang")
    @ExceptionHandler(ForbiddenException::class)
    fun tilgangsnekt() = Unit

}