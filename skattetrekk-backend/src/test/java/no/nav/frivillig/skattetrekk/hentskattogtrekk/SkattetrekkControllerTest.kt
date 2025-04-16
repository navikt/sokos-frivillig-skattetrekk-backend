//package no.nav.pensjon.selvbetjening.skattetrekk.skattetrekk
//
//import no.nav.pensjon.selvbetjening.skattetrekk.skattetrekk.model.Skattform
//import no.nav.virksomhet.tjenester.trekk.meldinger.v1.TrekkInfo
//import org.junit.Test
//import org.junit.jupiter.api.Assertions.*
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Disabled
//import org.junit.jupiter.api.extension.ExtendWith
//import org.mockito.Mockito
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration
//import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
//import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.boot.test.mock.mockito.MockBean
//import org.springframework.context.annotation.Import
//import org.springframework.http.HttpMethod
//import org.springframework.http.MediaType
//import org.springframework.test.context.junit.jupiter.SpringExtension
//import org.springframework.test.web.reactive.server.WebTestClient
//import org.springframework.web.reactive.function.BodyInserters
//import org.springframework.web.reactive.function.client.WebClient
//import java.time.LocalDate
//
//@ExtendWith(SpringExtension::class)
//@AutoConfigureWebTestClient
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Import(SkattetrekkController::class)
//@EnableAutoConfiguration(exclude = [SecurityAutoConfiguration::class])
//class SkattetrekkControllerTest(@Autowired val webClient: WebTestClient) {
//
//    @MockBean
//    lateinit var skattetrekkController: SkattetrekkController
//
//    @MockBean
//    lateinit var skattetrekkService: SkattetrekkService
//
//    @Disabled
//    @Test
//    fun getSkattetrekk() {
//
//        val trekkInfo1 = TrekkInfo()
//
//        val response = Skattform()
////        val validation: Validation = Validation(ValidationCode.NO_OMSORGSOPPTJENING_ON_GIVEN_YEAR, mapOf())
//        Mockito.`when`(skattetrekkService.getSkattetrekk()).thenReturn(listOf(validation))
//
////        val requestBody = ValidateOverforselRequest(
////            pidMottaker = "examplePidMottaker",
////            etternavnMottaker = "exampleNavn",
////            selectedYears = listOf(2022, 2023, 2024)
////        )
//
//        // Perform the GET request with the request body
//        webClient
//            .method(HttpMethod.GET)
//            .uri("/api/skattetrekk")
//            .contentType(MediaType.APPLICATION_JSON)
////            .body(BodyInserters.fromValue(requestBody))
//            .exchange()
//            .expectStatus().isOk
//            .expectBody()
//        // Assert any specific response content if needed
//    }
//
//    private fun <T> any(): T = Mockito.any()
//
//}