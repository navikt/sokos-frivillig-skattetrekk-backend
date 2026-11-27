Brukte KI til å forklare hva migrering ville innebære, som under.
Deretter tilsvarende i 3 steg: 
-  utgående klienter/kall,
-  autentisering og plugging/wiring
-  toppnivå med endepunkter og sikkerhet, pluss gradle-avhengigheter

Etter hvert steg:
-  kjørte tester
-  startet app lokalt
- hentet token fra https://tokenx-token-generator.intern.dev.nav.no/api/obo?aud=dev-gcp:skatt-og-trekk:sokos-frivillig-skattetrekk-backend-q2
- brukte token i lokalt kall til GET/POST
- post må gjøres først med 0 for å fjerne trekk, så ny verdi for å lage nytt trekk
- pushet og deployet til Q2
- testet fra frontend https://sokos-frivillig-skattetrekk-frontend-q2.intern.dev.nav.no/utbetalinger/frivillig-skattetrekk/

TODO push siste steg, test i Q2, legg PR til review
TODO kan bli nye metrics-navn, noen endrede feilmeldinger

TASK: Show me how to exchange spring framework with ktor

Configured authentication: Managed by agent

I’m examining the backend security components to understand how authentication, token validation, and user claims currently work, so I can identify what needs to carry over in the Spring-to-Ktor migration.

Subagent security-explorer

Fetch URL

Open file

You can migrate this application incrementally: keep the Kotlin business logic, and replace Spring’s HTTP, security, dependency injection, and client layers.

**1. Replace dependencies and startup**

In `build.gradle.kts`, remove the Spring Boot and Spring dependency-management plugins and starters **after** migrating their usages. Add Ktor dependencies using one consistent version:
```kotlin
val ktorVersion = "3.6.1"

implementation("io.ktor:ktor-server-netty:$ktorVersion")
implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
implementation("io.ktor:ktor-serialization-jackson3:$ktorVersion")
implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")
implementation("io.ktor:ktor-client-cio:$ktorVersion")
implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
```

Keep the `application` plugin and Java toolchain. Declare libraries currently supplied transitively by Spring, such as Nimbus JOSE JWT, explicitly where still needed. Check dependency compatibility and security updates before pinning versions.

Replace `Application.kt`’s `runApplication` with a Ktor server:

```kotlin
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}
```
`module()` installs plugins, constructs clients and services, and registers routes. This is a migration outline, not a complete drop-in implementation.
**2. Replace `SecurityConfiguration.kt`**

Your current configuration trusts the TokenX issuer and JWKS, allows 60 seconds of clock skew, protects `/api/**`, permits `/actuator/**`, and denies other requests. A Ktor authentication starting point is:

```kotlin
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.net.URI
import java.util.concurrent.TimeUnit

fun Application.configureSecurity() {
    val issuer = requireNotNull(System.getenv("TOKEN_X_ISSUER"))
    val jwksUri = requireNotNull(System.getenv("TOKEN_X_JWKS_URI"))
    val audience = requireNotNull(System.getenv("TOKEN_X_CLIENT_ID"))

    val jwks = JwkProviderBuilder(URI(jwksUri).toURL())
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        jwt("token-x") {
            verifier(jwks, issuer) {
                acceptLeeway(60)
                withAudience(audience)
            }
            validate { credential ->
                if (!credential.payload.subject.isNullOrBlank()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
```

This **adds** audience and nonblank subject checks: the attached Spring decoder does not explicitly enforce them. Confirm that `TOKEN_X_CLIENT_ID` is the correct expected audience for this deployment. Also compare expiry, not-before, issued-at, algorithms, and error responses with Spring before production; the libraries’ defaults need not be identical.

Register protected routes under `authenticate("token-x")`. Ktor does not automatically reproduce Spring’s `denyAll`, servlet error-dispatch rules, or CSRF behavior: explicitly decide and test the behavior of unknown routes, error responses, and POST requests. Do not expose a route simply because it was placed outside `authenticate`.

**3. Move the controller to routes**
`SkattetrekkController.kt` maps GET and POST to `/api/skattetrekk`. In Ktor, the shape becomes:

```kotlin
routing {
    authenticate("token-x") {
        route("/api/skattetrekk") {
            get {
                val pid = call.principal<JWTPrincipal>()!!.payload.subject
                // Also pass the validated request's bearer token through to
                // the refactored service/token-exchange code.
                val result = skattetrekkService.hentSkattetrekk(pid /*, tokenContext */)
                if (result == null) call.respond(HttpStatusCode.OK)
                else call.respond(result)
            }
            post {
                val pid = call.principal<JWTPrincipal>()!!.payload.subject
                val request = call.receive<BehandleRequest>()
                if (Validering.valider(request.value, request.satsType).isEmpty()) {
                    behandleTrekkService.behandleTrekk(
                        pid, request.value, request.satsType /*, tokenContext */
                    )
                }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
```

This is illustrative: the services still need the token-context refactor below. The existing POST silently returns success when validation fails; changing that to `400` would be

a separate API change. Use `StatusPages` to reproduce the controller’s exception-to-`400`/`403`/`503` mappings and response bodies. Configure Jackson 3 and test existing JSON, including `LocalDate` and nulls.

**4. Replace Spring wiring and outbound calls**

- Construct `TrekkClient`, `HentSkattOgTrekkService`, and `BehandleTrekkService` explicitly, or use a DI library; remove their Spring annotations.
- Replace `SecurityContextHolder`, `SetPidFilter`, and `SecurityContextUtil` with request-scoped data derived from the verified principal. Pass both the PID and the **original bearer token** explicitly through the service/client chain: `TokenService` needs the ingress token for TokenX exchange, not just the PID. Never log either value.
- Migrate `TrekkClient`, `TokenXService`, and `AzureAdService` from `WebClient` to Ktor `HttpClient`. Preserve TokenX exchange, Azure client credentials, form encoding, proxy configuration, timeouts, error mapping, and client lifecycle. Convert blocking calls to suspend functions or isolate blocking work from the server’s event-loop threads.
- Replace Spring configuration loading in `application.yaml` and `application-local.yaml`, including the local `defaults.properties` import; retain the deployed environment variables.

**5. Preserve operations and verify**

`.nais/dev/naiserator-q2.yaml` expects `/actuator/health/liveness`, `/actuator/health/readiness`, and `/actuator/prometheus` on port `8080`. Implement equivalent health checks and a Micrometer Prometheus scrape route, or update **both** deployment configurations. Compare metric names and labels before switching dashboards and alerts. Preserve the 16 KB header limit where required.

Keep the existing service unit tests; replace `MockMvc` tests with Ktor `testApplication` tests. Cover GET/POST behavior, error mapping, missing/invalid/expired tokens, issuer/audience/signature failures, clock skew, token exchange, and health/metrics endpoints. In particular, the current controller test disables filters, so it does not verify production authentication.

References: [Ktor JWT authentication](https://ktor.io/docs/server-jwt.html), [serialization](https://ktor.io/docs/server-serialization.html), and [Micrometer metrics](https://ktor.io/docs/server-metrics-micrometer.html). No project files were changed or tests run.