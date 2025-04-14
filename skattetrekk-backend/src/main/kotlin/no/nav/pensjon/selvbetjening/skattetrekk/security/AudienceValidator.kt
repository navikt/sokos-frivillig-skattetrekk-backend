package no.nav.pensjon.selvbetjening.skattetrekk.security

import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt

class AudienceValidator(val audience: String) : OAuth2TokenValidator<Jwt> {

    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult =
        if (jwt.audience.contains(audience))
            OAuth2TokenValidatorResult.success()
        else
            OAuth2TokenValidatorResult.failure(OAuth2Error("Invalid audience: ${jwt.audience.joinToString()}"))
}
