package no.nav.frivillig.skattetrekk.service

import no.nav.frivillig.skattetrekk.client.trekk.api.SatsType
import no.nav.frivillig.skattetrekk.endpoint.api.FrivilligSkattetrekkMessage
import no.nav.frivillig.skattetrekk.endpoint.api.FrivilligSkattetrekkMessageCode
import no.nav.frivillig.skattetrekk.endpoint.api.FrivilligSkattetrekkType

class Validering {

    companion object {
        const val MAX_BELOP = 50000 // Konstanten er avklart av fag
        const val MAX_PROSENT = 100 // Konstanten er avklart av fag

        fun valider(tilleggstrekk: Int, satsType: SatsType): List<FrivilligSkattetrekkMessage> {
            val valideringsListe = mutableListOf<FrivilligSkattetrekkMessage>()
            if (satsType == SatsType.KRONER) {
                if (tilleggstrekk > MAX_BELOP) {
                    valideringsListe.add(
                        FrivilligSkattetrekkMessage(
                            code = FrivilligSkattetrekkMessageCode.MAX_BELOP_OVERSTEGET,
                            type = FrivilligSkattetrekkType.ERROR
                        )
                    )
                }
                if (tilleggstrekk < 0) {
                    valideringsListe.add(
                        FrivilligSkattetrekkMessage(
                            code = FrivilligSkattetrekkMessageCode.MIN_BELOP,
                            type = FrivilligSkattetrekkType.ERROR
                        )
                    )
                }
            } else {
                if (tilleggstrekk > MAX_PROSENT) {
                    valideringsListe.add(
                        FrivilligSkattetrekkMessage(
                            code = FrivilligSkattetrekkMessageCode.MAX_PROSENT_OVERSTEGET,
                            type = FrivilligSkattetrekkType.ERROR
                        )
                    )
                }
                if (tilleggstrekk < 0) {
                    valideringsListe.add(
                        FrivilligSkattetrekkMessage(
                            code = FrivilligSkattetrekkMessageCode.MIN_PROSENT,
                            type = FrivilligSkattetrekkType.ERROR
                        )
                    )
                }
            }
            return valideringsListe
        }
    }

}