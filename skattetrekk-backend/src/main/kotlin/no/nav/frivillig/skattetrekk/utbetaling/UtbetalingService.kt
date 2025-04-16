package no.nav.frivillig.skattetrekk.utbetaling

import org.springframework.stereotype.Service

@Service
class UtbetalingService(
    val utbetalingClient: UtbetalingClient
) {

    fun harUtbetaling(fnr: String): Boolean {
        val utbetaling = utbetalingClient
            .fetchUtbetalinger(fnr)
            .maxByOrNull { it.posteringsdato }
        return utbetaling != null
    }
}