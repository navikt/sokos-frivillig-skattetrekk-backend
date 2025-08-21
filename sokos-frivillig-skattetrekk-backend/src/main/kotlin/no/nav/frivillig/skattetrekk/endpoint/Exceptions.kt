package no.nav.frivillig.skattetrekk.endpoint

open class PersonNotFoundException(
    override val system: String,
    override val service: String,
    override val message: String?,
    override val cause: Throwable?,
) : ClientException(system, service, message, cause)

open class ClientException(
    open val system: String,
    open val service: String,
    override val message: String?,
    override val cause: Throwable?,
) : RuntimeException("Error occurred when calling service $service in $system. DetailMessage:  $message", cause)

open class ForbiddenException(
    val system: String,
    val service: String,
    override val message: String?,
    override val cause: Throwable?,
) : RuntimeException("Access denied when calling service $service in $system. DetailMessage:  $message", cause)

class OppdragUtilgjengeligException : RuntimeException()

class TekniskFeilFraOppdragException : RuntimeException()

class LogInLevelTooLowException : RuntimeException()

class UnauthorizedException : RuntimeException()
