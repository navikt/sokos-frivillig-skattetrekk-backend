package no.nav.frivillig.skattetrekk.utbetaling

import no.nav.pensjon.selvbetjening.skattetrekk.utbetaling.model.Utbetaling
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*


@Service
class UtbetalingService(
    val utbetalingClient: UtbetalingClient
) {

    companion object {
        private const val NAV_UTBETALING_UFORE = "uføretrygd"
        private const val NAV_UTBETALING_PENSJON = "alderspensjon"
    }

    fun harUtbetaling(fnr: String): Boolean {
        val utbetaling = utbetalingClient
            .fetchUtbetalinger(fnr)
            .maxByOrNull { it.posteringsdato }
        return utbetaling != null
    }

}