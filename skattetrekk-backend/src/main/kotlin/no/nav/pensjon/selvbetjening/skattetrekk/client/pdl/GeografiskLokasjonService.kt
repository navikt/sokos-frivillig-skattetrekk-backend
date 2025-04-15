package no.nav.pensjon.selvbetjening.skattetrekk.client.pdl

import no.nav.pensjon.selvbetjening.skattetrekk.client.norg2.Norg2Client
import no.nav.pensjon.selvbetjening.skattetrekk.client.pdl.api.AdressebeskyttelseGradering
import org.springframework.stereotype.Service

@Service
class GeografiskLokasjonService(
    val pdlClient: PdlClient,
    val norg2Client: Norg2Client
) {

    fun hentNavEnhet(pid: String): String {
        val geografiskTilknytningOgAdressebeskyttelse = pdlClient.hentGeografiskTilknytningOgAdresseBeskyttelseQuery(pid)
        val diskresjon = geografiskTilknytningOgAdressebeskyttelse
            ?.data?.hentPerson?.adressebeskyttelse?.firstOrNull()?.gradering
            ?: AdressebeskyttelseGradering.UGRADERT
        return norg2Client.hentEnhetForSpesifisertGeografiskOmraade(
            pid,
            geografiskTilknytningOgAdressebeskyttelse?.data?.hentGeografiskTilknytning?.gtKommune!!,
            DiskresjonMapper.fromPdlAdressebeskyttelseGradering(diskresjon))
    }
}