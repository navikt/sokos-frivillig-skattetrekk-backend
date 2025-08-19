package no.nav.frivillig.skattetrekk

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SkattetrekkApplication

fun main(args: Array<String>) {
    runApplication<SkattetrekkApplication>(*args)
}