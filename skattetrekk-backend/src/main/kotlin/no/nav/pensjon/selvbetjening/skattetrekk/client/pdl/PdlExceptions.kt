package no.nav.pensjon.selvbetjening.skattetrekk.client.pdl

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.pensjon.selvbetjening.skattetrekk.client.pdl.api.PdlError
import no.nav.pensjon.selvbetjening.skattetrekk.client.util.Errorhandler.Companion.handleResponseStatusException
import org.springframework.http.HttpStatus

class PersonNotFoundInPdlException(message: String) : RuntimeException(message)

class UnauthenticatedForPdlException(message: String) : RuntimeException(message)

class UnauthorizedForAccessToPersonInPdlException(message: String) : RuntimeException(message)

class PdlBadRequestException(message: String) : RuntimeException(message)

class PdlServerErrorException(message: String) : RuntimeException(message)

class PdlException(override val cause: Throwable) : RuntimeException("An error occurred when getting persondata from PDL", cause)

class PdlErrorResponseException(val errors: List<PdlError>) : RuntimeException(convertErrorsToErrorMessage(errors)){
    companion object{
        fun convertErrorsToErrorMessage(errors: List<PdlError>): String{
            val mapper = ObjectMapper()
            return mapper.writeValueAsString(errors)
        }
    }
}

fun handlePdlErrors(pid: String, exception: Throwable) {
    when (exception) {
        is PersonNotFoundInPdlException -> throw handleResponseStatusException(
            HttpStatus.NOT_FOUND, pid, exception
        )

        is UnauthenticatedForPdlException -> throw handleResponseStatusException(
            HttpStatus.UNAUTHORIZED, pid, exception
        )

        is UnauthorizedForAccessToPersonInPdlException -> throw handleResponseStatusException(
            HttpStatus.FORBIDDEN, pid, exception
        )

        is PdlBadRequestException,
        is PdlServerErrorException,
            -> throw handleResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, pid, exception)

        else -> throw handleResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, pid, exception)
    }
}