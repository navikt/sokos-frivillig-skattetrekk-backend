package no.nav.sokos.frivillig.skattetrekk.backend.util

import java.time.LocalDate

fun isDateInPeriod(
    compDate: LocalDate,
    fomDate: LocalDate?,
    tomDate: LocalDate?,
): Boolean = (fomDate == null || !compDate.isBefore(fomDate)) && (tomDate == null || !compDate.isAfter(tomDate))
