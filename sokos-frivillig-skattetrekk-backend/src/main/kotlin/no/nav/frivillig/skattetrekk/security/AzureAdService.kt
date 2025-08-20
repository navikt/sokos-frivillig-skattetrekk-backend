package no.nav.frivillig.skattetrekk.security

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient

@Service
class AzureAdService(
    @Value("\${oauth2.azureAd.clientId}") private val clientId: String,
    @Value("\${oauth2.azureAd.clientSecret}") private val clientSecret: String,
    @Value("\${oauth2.azureAd.tokenEndpoint}") private val endpoint: String,
    @Qualifier("webClientProxy") private val webClient: WebClient,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ClientCredentialsTokenResponse(
        @JsonProperty("access_token") val accessToken: String,
    )

    fun retrieveClientCredentialsToken(scope: List<String>): String? =
        webClient
            .post()
            .uri(endpoint)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(
                LinkedMultiValueMap<String, String>().apply {
                    add("grant_type", "client_credentials")
                    add("client_id", clientId)
                    add("client_secret", clientSecret)
                    add("scope", scope.joinToString(" "))
                },
            ).retrieve()
            .bodyToMono(ClientCredentialsTokenResponse::class.java)
            .block()
            ?.accessToken
}
