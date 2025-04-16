package no.nav.frivillig.skattetrekk.endpoint

import no.nav.frivillig.skattetrekk.endpoint.api.FrivilligSkattetrekkInitResponse
import no.nav.frivillig.skattetrekk.endpoint.api.SaveFrivilligSkattetrekkRequest
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
        val skatteTrekk =  skattetrekkService.getSkattetrekk(SecurityContextUtil.getPidFromContext())
        return ResponseEntity<FrivilligSkattetrekkInitResponse?>(skatteTrekk, HttpStatus.OK)
    }

    @PostMapping(consumes = ["application/json"])
    fun saveFrivilligSkattetrekk(@RequestBody saveFrivilligSkattetrekkRequest: SaveFrivilligSkattetrekkRequest) {
        behandleTrekkService.behandleTrekk(
            saveFrivilligSkattetrekkRequest.trekkVedtakId,
            saveFrivilligSkattetrekkRequest.value,
            saveFrivilligSkattetrekkRequest.satsType)
    }

}