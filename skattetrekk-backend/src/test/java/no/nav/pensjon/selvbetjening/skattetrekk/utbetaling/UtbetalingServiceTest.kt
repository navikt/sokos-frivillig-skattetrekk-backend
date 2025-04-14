package no.nav.pensjon.selvbetjening.skattetrekk.utbetaling

import no.nav.pensjon.selvbetjening.skattetrekk.utbetaling.model.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class UtbetalingServiceTest {

    @Mock
    private lateinit var utbetalingClient: UtbetalingClient

    @InjectMocks
    private lateinit var utbetalingService: UtbetalingService

    @Test
    fun `harUtbetaling should return true when there is a valid utbetaling`() {
        val fnr = "12345678901"
        val utbetaling = mockUtbetaling()
        `when`(utbetalingClient.fetchUtbetalinger(fnr)).thenReturn(listOf(utbetaling))

        val result = utbetalingService.harUtbetaling(fnr)
        assertTrue(result)
    }

    @Test
    fun `harUtbetaling should return true when there is utbetaling of other ytelsestype`() {
        val fnr = "12345678901"
        val utbetaling = mockUtbetaling("sykepenger")
        `when`(utbetalingClient.fetchUtbetalinger(fnr)).thenReturn(listOf(utbetaling))

        val result = utbetalingService.harUtbetaling(fnr)
        assertTrue(result)
    }

    fun mockYtelse(ytelsestype: String? = "alderspensjon"): Ytelse {
        return Ytelse(
            ytelsestype = ytelsestype,
            ytelsesperiode = Periode(LocalDate.now().minusMonths(1), LocalDate.now()),
            ytelseNettobeloep = BigDecimal("1000.00"),
            rettighetshaver = Aktoer(Aktoertype.PERSON, "12345678901", "Mock Navn"),
            skattsum = BigDecimal("100.00"),
            trekksum = BigDecimal("50.00"),
            ytelseskomponentersum = BigDecimal("1150.00"),
            skattListe = listOf(Skatt(BigDecimal("100.00"))),
            trekkListe = listOf(Trekk("mockTrekktype", BigDecimal("50.00"), "mockKreditor")),
            ytelseskomponentListe = listOf(Ytelseskomponent("mockType", BigDecimal("100.00"), "mockSatstype", 1.0, BigDecimal("100.00"))),
            bilagsnummer = "mockBilagsnummer",
            refundertForOrg = Aktoer(Aktoertype.ORGANISASJON, "987654321", "Mock Org")
        )
    }

    fun mockUtbetaling(ytelsestype: String? = "alderspensjon"): Utbetaling {
        return Utbetaling(
            posteringsdato = LocalDate.now(),
            ytelseListe = listOf(mockYtelse(ytelsestype)),
            utbetaltTil = Aktoer(Aktoertype.PERSON, "12345678901", "Mock Navn"),
            utbetalingsmetode = "mockUtbetalingsmetode",
            utbetalingsstatus = "mockUtbetalingsstatus",
            forfallsdato = LocalDate.now().plusMonths(1),
            utbetalingsdato = LocalDate.now().plusDays(2),
            utbetalingNettobeloep = BigDecimal("1000.00"),
            utbetalingsmelding = "mockUtbetalingsmelding",
            utbetaltTilKonto = Bankkonto("mockKontonummer", "mockKontotype")
        )
    }
}