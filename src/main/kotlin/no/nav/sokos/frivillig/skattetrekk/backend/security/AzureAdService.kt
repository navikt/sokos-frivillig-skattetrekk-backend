package no.nav.sokos.frivillig.skattetrekk.backend.security

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.parameters
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AzureAdService(
    @Value("\${oauth2.azureAd.clientId}")
    private val clientId: String,
    @Value("\${oauth2.azureAd.clientSecret}")
    private val clientSecret: String,
    @Value("\${oauth2.azureAd.tokenEndpoint}")
    private val endpoint: String,
    @Qualifier("webClientProxy")
    private val webClient: HttpClient,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ClientCredentialsTokenResponse(
        @JsonProperty("access_token") val accessToken: String,
    )

    fun retrieveClientCredentialsToken(scope: List<String>): String? =
        runBlocking {
            val response = webClient.submitForm(
                url = endpoint,
                formParameters = parameters {
                    append("grant_type", "client_credentials")
                    append("client_id", clientId)
                    append("client_secret", clientSecret)
                    append("scope", scope.joinToString(" "))
                },
            ) {
                expectSuccess = false
            }
            val body = response.bodyAsText()
            when (response.status.value) {
                in 400..499 -> throw ClientRequestException(response, body)
                in 500..599 -> throw ServerResponseException(response, body)
            }
            if (body.isEmpty()) null else response.body<ClientCredentialsTokenResponse>().accessToken
        }
}
