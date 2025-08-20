package no.nav.frivillig.skattetrekk.client.pdl

import no.nav.frivillig.skattetrekk.client.pdl.api.AdressebeskyttelseGradering

class DiskresjonMapper {
    companion object {
        fun fromPdlAdressebeskyttelseGradering(adressebeskyttelseGradering: AdressebeskyttelseGradering?): String =
            when (adressebeskyttelseGradering) {
                AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND, AdressebeskyttelseGradering.STRENGT_FORTROLIG -> "SPSF"
                AdressebeskyttelseGradering.FORTROLIG -> "SPFO"
                AdressebeskyttelseGradering.UGRADERT -> "ANY"
                else -> "ANY"
            }
    }
}
