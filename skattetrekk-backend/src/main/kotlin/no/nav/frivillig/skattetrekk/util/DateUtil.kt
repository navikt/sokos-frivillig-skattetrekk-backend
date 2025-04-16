package no.nav.frivillig.skattetrekk.util

import java.util.*

fun isDateInPeriod(compDate: Date?, fomDate: Date?, tomDate: Date?): Boolean {
    return (fomDate == null || compDate!!.compareTo(fomDate) >= 0) && (tomDate == null || compDate!!.compareTo(tomDate) <= 0) // todo I dont know if this is correct
}