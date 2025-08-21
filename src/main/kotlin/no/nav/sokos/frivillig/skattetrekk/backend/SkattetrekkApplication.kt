package no.nav.sokos.frivillig.skattetrekk.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SkattetrekkApplication

fun main(args: Array<String>) {
    runApplication<no.nav.sokos.frivillig.skattetrekk.backend.SkattetrekkApplication>(*args)
}
