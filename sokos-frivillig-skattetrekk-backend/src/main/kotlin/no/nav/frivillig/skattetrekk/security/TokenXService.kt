package no.nav.frivillig.skattetrekk.security

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import java.time.Instant
import java.util.Date
import java.util.UUID

@Service
class TokenXService(
    @Value("\${oauth2.tokenX.tokenEndpoint}") private val endpoint: String,
    @Value("\${oauth2.tokenX.privateJwk}") private val privateJwk: String,
    @Value("\${oauth2.tokenX.clientId}") private val clientId: String,
    @Qualifier("webClientProxy") private val webClient: WebClient,
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
        @JsonProperty(value = "access_token", required = true) val accessToken: String,
        @JsonProperty(value = "token_type", required = true) val tokenType: String,
        @JsonProperty(value = "expires_in", required = true) val expiresIn: Int,
    )

    fun exchangeIngressTokenToEgressToken(
        subjectToken: String,
        audience: String,
    ): String? =
        webClient
            .post()
            .uri(endpoint)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(
                LinkedMultiValueMap<String, String>().apply {
                    add(PARAMS_GRANT_TYPE, GRANT_TYPE)
                    add(PARAMS_SUBJECT_TOKEN_TYPE, SUBJECT_TOKEN_TYPE)
                    add(PARAMS_SUBJECT_TOKEN, subjectToken)
                    add(PARAMS_AUDIENCE, audience)
                    add(PARAMS_CLIENT_ASSERTION, createAssertionToken())
                    add(PARAMS_CLIENT_ASSERTION_TYPE, CLIENT_ASSERTION_TYPE)
                },
            ).retrieve()
            .bodyToMono(TokenXResponse::class.java)
            .block()
            ?.accessToken

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
