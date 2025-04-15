package no.nav.pensjon.selvbetjening.skattetrekk.client.pdl

import no.nav.pensjon.selvbetjening.skattetrekk.client.pdl.api.AdressebeskyttelseGradering

class DiskresjonMapper {
    companion object {
        fun fromPdlAdressebeskyttelseGradering(adressebeskyttelseGradering: AdressebeskyttelseGradering?): String {
            return when (adressebeskyttelseGradering) {
                AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND, AdressebeskyttelseGradering.STRENGT_FORTROLIG -> "SPSF"
                AdressebeskyttelseGradering.FORTROLIG -> "SPFO"
                AdressebeskyttelseGradering.UGRADERT -> "ANY"
                else -> "ANY"
            }
        }
    }
}