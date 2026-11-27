package no.nav.sokos.frivillig.skattetrekk.backend.security

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

fun JwtAuthenticationToken.toRequestContext(): RequestContext =
    RequestContext(
        pid = name,
        issuer = token.getClaim<String>("iss"),
        acr = token.getClaim<String>("acr"),
        bearerToken = token.tokenValue,
    )