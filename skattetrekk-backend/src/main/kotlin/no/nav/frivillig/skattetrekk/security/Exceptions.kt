package no.nav.frivillig.skattetrekk.security

class NoFullmaktPresentException : RuntimeException()
class LoginLevelTooLowException : RuntimeException()
class UnauthorizedException : RuntimeException()