package no.nav.pensjon.selvbetjening.skattetrekk.utbetaling.model

class UtbetalDataApiFeil(message: String): Exception(message) {}

class UtbetalDataServerFeil(message: String): Exception(message) {}

class UtbetalDataIkkeFunnet(message: String): Exception(message) {}