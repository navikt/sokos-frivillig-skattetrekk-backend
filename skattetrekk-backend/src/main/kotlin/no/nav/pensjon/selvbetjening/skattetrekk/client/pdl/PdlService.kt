package no.nav.pensjon.selvbetjening.skattetrekk.client.pdl

import no.nav.pensjon.selvbetjening.skattetrekk.client.norg2.Norg2Client
import no.nav.pensjon.selvbetjening.skattetrekk.client.pdl.dto.GeografiskLokasjon
import org.springframework.stereotype.Service

@Service
class PdlService(
    val pdlClient: PdlClient,
    val norg2Client: Norg2Client
) {

    fun hentGeografiskTilknytningOgAdressebeskyttelse(pid: String): GeografiskLokasjon {
        val geografiskTilknytningOgAdressebeskyttelse = pdlClient.hentGeografiskTilknytningOgAdresseBeskyttelseQuery(pid)
        val diskresjon = geografiskTilknytningOgAdressebeskyttelse?.data?.hentPerson?.adressebeskyttelse?.gradering?.name
        val enhetOgGeografiskOmraade = norg2Client.hentEnhetForSpesifisertGeografiskOmraade(
            pid,
            geografiskTilknytningOgAdressebeskyttelse?.data?.hentGeografiskTilknytning?.gtKommune!!,
            diskresjon!!)
        return GeografiskLokasjon(enhetOgGeografiskOmraade, diskresjon)
    }
}