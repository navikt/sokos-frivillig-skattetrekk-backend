package no.nav.sokos.frivillig.skattetrekk.backend.config

import java.nio.file.Path
import java.util.Properties

class ApplicationConfig(
    val activeProfiles: List<String>,
    val azureAd: AzureAdConfig,
    val tokenX: TokenXConfig,
    val trekk: TrekkConfig,
    val httpProxy: String?,
) {
    companion object {
        fun fromEnvironment(
            environment: Map<String, String> = System.getenv(),
            localDefaults: Path = Path.of("defaults.properties"),
        ): ApplicationConfig {
            val profiles = (environment["SPRING_PROFILES_ACTIVE"] ?: "local")
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val defaults = Properties()
            if ("local" in profiles) {
                localDefaults.toFile().inputStream().use { defaults.load(it) }
            }
            fun required(name: String): String =
                environment[name] ?: defaults.getProperty(name)
                ?: throw IllegalStateException("Missing required configuration: $name")

            return ApplicationConfig(
                activeProfiles = profiles,
                azureAd = AzureAdConfig(
                    clientId = required("AZURE_APP_CLIENT_ID"),
                    clientSecret = required("AZURE_APP_CLIENT_SECRET"),
                    issuer = required("AZURE_OPENID_CONFIG_ISSUER"),
                    jsonWebKeyUri = required("AZURE_OPENID_CONFIG_JWKS_URI"),
                    tokenEndpoint = required("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
                ),
                tokenX = TokenXConfig(
                    clientId = required("TOKEN_X_CLIENT_ID"),
                    issuer = required("TOKEN_X_ISSUER"),
                    jsonWebKeyUri = required("TOKEN_X_JWKS_URI"),
                    tokenEndpoint = required("TOKEN_X_TOKEN_ENDPOINT"),
                    privateJwk = required("TOKEN_X_PRIVATE_JWK"),
                ),
                trekk = TrekkConfig(
                    url = required("TREKK_URL"),
                    scope = required("TREKK_SCOPE"),
                    audience = required("TREKK_AUDIENCE"),
                ),
                httpProxy = environment["HTTP_PROXY"],
            )
        }
    }
}

class AzureAdConfig(
    val clientId: String,
    val clientSecret: String,
    val issuer: String,
    val jsonWebKeyUri: String,
    val tokenEndpoint: String,
)

class TokenXConfig(
    val clientId: String,
    val issuer: String,
    val jsonWebKeyUri: String,
    val tokenEndpoint: String,
    val privateJwk: String,
)

class TrekkConfig(
    val url: String,
    val scope: String,
    val audience: String,
)