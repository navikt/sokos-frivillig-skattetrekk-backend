package no.nav.pensjon.selvbetjening.skattetrekk.security

class NoFullmaktPresentException : RuntimeException()
class LoginLevelTooLowException : RuntimeException()
class UnauthorizedException : RuntimeException()