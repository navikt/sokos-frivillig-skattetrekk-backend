package no.nav.sokos.frivillig.skattetrekk.backend.security

import java.time.Instant
import java.util.Date
import java.util.UUID

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.parameters
import kotlinx.coroutines.runBlocking
import tools.jackson.databind.ObjectMapper

class TokenXService(
    private val endpoint: String,
    private val privateJwk: String,
    private val clientId: String,
    private val httpClient: HttpClient,
) {
    companion object {
        internal const val PARAMS_GRANT_TYPE = "grant_type"
        internal const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:token-exchange"
        internal const val PARAMS_SUBJECT_TOKEN_TYPE = "subject_token_type"
        internal const val SUBJECT_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:jwt"
        internal const val PARAMS_SUBJECT_TOKEN = "subject_token"
        internal const val PARAMS_AUDIENCE = "audience"
        internal const val PARAMS_CLIENT_ASSERTION = "client_assertion"
        internal const val PARAMS_CLIENT_ASSERTION_TYPE = "client_assertion_type"
        internal const val CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TokenXJwkResponse(
        @JsonProperty(value = "kid", required = true) val kid: String,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TokenXResponse(
        @JsonProperty(value = "access_token", required = true)
        val accessToken: String,
        @JsonProperty(value = "token_type", required = true)
        val tokenType: String,
        @JsonProperty(value = "expires_in", required = true)
        val expiresIn: Int,
    )

    fun exchangeIngressTokenToEgressToken(
        subjectToken: String,
        audience: String,
    ): String? = runBlocking {
        val response = httpClient.submitForm(
            url = endpoint,
            formParameters = parameters {
                append(PARAMS_GRANT_TYPE, GRANT_TYPE)
                append(PARAMS_SUBJECT_TOKEN_TYPE, SUBJECT_TOKEN_TYPE)
                append(PARAMS_SUBJECT_TOKEN, subjectToken)
                append(PARAMS_AUDIENCE, audience)
                append(PARAMS_CLIENT_ASSERTION, createAssertionToken())
                append(PARAMS_CLIENT_ASSERTION_TYPE, CLIENT_ASSERTION_TYPE)
            },
        )
        val responseText = response.bodyAsText()
        when (response.status.value) {
            in 400..499 -> throw ClientRequestException(response, responseText)
            in 500..599 -> throw ServerResponseException(response, responseText)
        }
        if (responseText.isEmpty()) null else response.body<TokenXResponse>().accessToken
    }

    private fun createAssertionToken(): String {
        val now = Date.from(Instant.now())
        val jwt =
            JWTClaimsSet
                .Builder()
                .subject(clientId)
                .issuer(clientId)
                .audience(endpoint)
                .jwtID(UUID.randomUUID().toString())
                .notBeforeTime(now)
                .issueTime(now)
                .expirationTime(Date.from(Instant.now().plusSeconds(30)))
                .build()

        val privateKey = RSAKey.parse(privateJwk)

        val signedJwt =
            SignedJWT(
                JWSHeader
                    .Builder(JWSAlgorithm.RS256)
                    .keyID(ObjectMapper().readValue(privateJwk, TokenXJwkResponse::class.java).kid)
                    .type(JOSEObjectType.JWT)
                    .build(),
                jwt,
            ).apply { sign(RSASSASigner(privateKey.toRSAPrivateKey())) }

        return signedJwt.serialize()
    }
}
