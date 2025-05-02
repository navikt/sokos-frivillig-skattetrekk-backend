package no.nav.frivillig.skattetrekk.endpoint

import no.nav.frivillig.skattetrekk.endpoint.api.*
import no.nav.frivillig.skattetrekk.security.SecurityContextUtil
import no.nav.frivillig.skattetrekk.service.BehandleTrekkService
import no.nav.frivillig.skattetrekk.service.HentSkattOgTrekkService
import org.springframework.http.HttpStatus
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

        return OppdaterResponse(trekkVedtakId = trekkvedtakId)
    }

    @DeleteMapping(consumes = ["application/json"])
    fun opphoerFrivilligSkattetrekk(@RequestBody request: OpphoerRequest) {
        behandleTrekkService.opphoerTrekk(SecurityContextUtil.getPidFromContext(), request.trekkVedtakId)
    }

}