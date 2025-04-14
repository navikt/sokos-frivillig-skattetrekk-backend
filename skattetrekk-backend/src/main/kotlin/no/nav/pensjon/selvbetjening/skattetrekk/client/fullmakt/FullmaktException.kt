package no.nav.pensjon.selvbetjening.skattetrekk.client.fullmakt

class FullmaktException : RuntimeException {
    constructor(
        serviceProvider: String,
        serviceIdentifier: String,
        detailMessage: String,
        cause: Throwable?
    ) : super("Error when calling the external service $serviceIdentifier in $serviceProvider. $detailMessage", cause)

    constructor(
        serviceProvider: String,
        detailMessage: String
    ) : super("Error when calling the external service $serviceProvider. $detailMessage")

    constructor(
        serviceProvider: String,
        cause: Throwable?
    ) : super("Error when calling the external service $serviceProvider", cause)
}