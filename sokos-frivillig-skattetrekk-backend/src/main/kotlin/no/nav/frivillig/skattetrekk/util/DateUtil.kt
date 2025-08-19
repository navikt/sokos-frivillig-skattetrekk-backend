package no.nav.frivillig.skattetrekk.util

import java.time.LocalDate

fun isDateInPeriod(compDate: LocalDate, fomDate: LocalDate?, tomDate: LocalDate?): Boolean {
    return (fomDate == null || !compDate.isBefore(fomDate)) && (tomDate == null || !compDate.isAfter(tomDate))
}