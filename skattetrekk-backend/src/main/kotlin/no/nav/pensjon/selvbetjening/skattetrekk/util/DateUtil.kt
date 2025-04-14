package no.nav.pensjon.selvbetjening.skattetrekk.util

import java.time.LocalDate
import java.util.*

fun isDateInPeriod(compDate: Date?, fomDate: Date?, tomDate: Date?): Boolean {
    return (fomDate == null || compDate!!.compareTo(fomDate) >= 0) && (tomDate == null || compDate!!.compareTo(tomDate) <= 0) // todo I dont know if this is correct
}

fun isSameDay(thisDate: Date?, thatDate: Date?): Boolean {
    if (thisDate == null && thatDate == null) { //todo what to do if one date is null? in pselv this is not null safe
        return false
    }

    return thatDate!!.equals(thisDate)
}

fun getLastDayOfMonth(): Int {
    val calendar: Calendar = Calendar.getInstance()

    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}