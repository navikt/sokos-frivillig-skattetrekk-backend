package no.nav.pensjon.selvbetjening.skattetrekk.skattetrekk

import no.nav.pensjon.selvbetjening.skattetrekk.service.BehandleTrekkService
import no.nav.pensjon.selvbetjening.skattetrekk.service.HentSkattOgTrekkService
import no.nav.pensjon.selvbetjening.skattetrekk.skattetrekk.api.FrivilligSkattetrekkInitResponse
import no.nav.pensjon.selvbetjening.skattetrekk.security.SecurityContextUtil
import no.nav.pensjon.selvbetjening.skattetrekk.skattetrekk.api.SaveFrivilligSkattetrekkRequest
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