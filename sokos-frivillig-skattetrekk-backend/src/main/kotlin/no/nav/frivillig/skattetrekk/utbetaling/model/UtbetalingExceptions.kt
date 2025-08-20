package no.nav.frivillig.skattetrekk.utbetaling.model

class UtbetalDataApiFeil(
    message: String,
) : Exception(message)

class UtbetalDataServerFeil(
    message: String,
) : Exception(message)

class UtbetalDataIkkeFunnet(
    message: String,
) : Exception(message)
