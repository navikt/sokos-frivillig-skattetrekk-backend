package no.nav.sokos.frivillig.skattetrekk.backend.security

class RequestContext(
    val pid: String,
    val issuer: String?,
    val acr: String?,
    val bearerToken: String,
) {
    override fun toString(): String = "RequestContext([redacted])"
}