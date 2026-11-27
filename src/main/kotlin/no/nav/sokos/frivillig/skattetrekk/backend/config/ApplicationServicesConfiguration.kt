package no.nav.sokos.frivillig.skattetrekk.backend.config

import io.ktor.client.HttpClient

import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.TrekkClient
import no.nav.sokos.frivillig.skattetrekk.backend.security.TokenService
import no.nav.sokos.frivillig.skattetrekk.backend.service.BehandleTrekkService
import no.nav.sokos.frivillig.skattetrekk.backend.service.HentSkattOgTrekkService

class ApplicationServicesConfiguration {
    fun trekkClient(
        url: String,
        scope: String,
        audience: String,
        tokenService: TokenService,
        httpClient: HttpClient,
    ) = TrekkClient(url, scope, audience, tokenService, httpClient)

    fun hentSkattOgTrekkService(trekkClient: TrekkClient) = HentSkattOgTrekkService(trekkClient)

    fun behandleTrekkService(trekkClient: TrekkClient) = BehandleTrekkService(trekkClient)
}