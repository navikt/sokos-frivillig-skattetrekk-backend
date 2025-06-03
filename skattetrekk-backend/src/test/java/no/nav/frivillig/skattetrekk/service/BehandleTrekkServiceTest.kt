package no.nav.frivillig.skattetrekk.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.frivillig.skattetrekk.client.trekk.TrekkClient
import no.nav.frivillig.skattetrekk.client.trekk.api.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BehandleTrekkServiceTest {

    private val pid = "12345678910"
    private val trekkClientMock = mockk<TrekkClient>()
    private val geografiskLokasjonServiceMock = mockk<GeografiskLokasjonService>()
    private val behandleTrekkService = BehandleTrekkService(trekkClientMock, geografiskLokasjonServiceMock)

    @Test
    fun `Opphor trekk dersom ett løpende trekk via behandle trekk`() {

        val trekkvedtakId = 1L

        every { trekkClientMock.finnTrekkListe(pid, any()) } returns listOf(
            lagTrekkInfo(
                trekkvedtakId = trekkvedtakId,
                sats = BigDecimal.valueOf(20),
                ansvarligEnhetId = "NAV Enhet",
                fom = LocalDate.now().minusMonths(1L),
                tom = null
            ),
        )
        every { trekkClientMock.opphorAndreTrekk(eq(pid), any()) } returns Unit
        every { geografiskLokasjonServiceMock.hentNavEnhet(pid) } returns "NAV Enhet"
        every { trekkClientMock.hentSkattOgTrekk(pid, trekkvedtakId) } returns lagHentSkattOgTrekkRespons(trekkvedtakId, listOf(
            lagSatsperiode(
                fom = LocalDate.now().minusMonths(1L),
                tom = LocalDate.now().plusMonths(3L),
                sats = 100.0
            )
        ))

        behandleTrekkService.behandleTrekk(pid,0, SatsType.KRONER)

        verify(exactly = 1) {
            trekkClientMock.opphorAndreTrekk(
                eq(pid),
                any()
            )
        }

    }

    @Test
    fun `Opprett trekk dersom tom trekkliste via behandle trekk`() {

        val trekkvedtakId = 1L

        every { trekkClientMock.finnTrekkListe(pid, any()) } returns emptyList()
        every { trekkClientMock.opprettAndreTrekk(eq(pid), any()) } returns OpprettAndreTrekkResponse(trekkvedtakId)
        every { geografiskLokasjonServiceMock.hentNavEnhet(pid) } returns "NAV Enhet"
        every { trekkClientMock.hentSkattOgTrekk(pid, trekkvedtakId) } returns lagHentSkattOgTrekkRespons(trekkvedtakId, listOf(
            lagSatsperiode(
                fom = LocalDate.now().minusMonths(1L),
                tom = LocalDate.now().plusMonths(3L),
                sats = 100.0
            )
        ))

        behandleTrekkService.behandleTrekk(pid,40, SatsType.PROSENT)

        verify(exactly = 0) { trekkClientMock.opphorAndreTrekk(eq(pid), any()) }
        verify(exactly = 1) { trekkClientMock.opprettAndreTrekk(eq(pid), any()) }
    }

    @Test
    fun `Opphor trekk dersom ett løpende trekk og ikke gjøre noe med ett lukket via behandle trekk`() {

        val trekkvedtakId = 1L
        val trekkvedtakId2 = 2L

        every { trekkClientMock.finnTrekkListe(pid, any()) } returns listOf(
            lagTrekkInfo(
                trekkvedtakId = trekkvedtakId,
                sats = BigDecimal.valueOf(20),
                ansvarligEnhetId = "NAV Enhet",
                fom = LocalDate.now().minusMonths(1L),
                tom = null
            ),
            lagTrekkInfo(
                trekkvedtakId = trekkvedtakId2,
                sats = BigDecimal.valueOf(20),
                ansvarligEnhetId = "NAV Enhet",
                fom = LocalDate.now().minusMonths(5L),
                tom = LocalDate.now().minusMonths(3L)
            )
        )
        every { trekkClientMock.opphorAndreTrekk(eq(pid), any()) } returns Unit
        every { geografiskLokasjonServiceMock.hentNavEnhet(pid) } returns "NAV Enhet"
        every { trekkClientMock.hentSkattOgTrekk(pid, trekkvedtakId) } returns lagHentSkattOgTrekkRespons(trekkvedtakId, listOf(
            lagSatsperiode(
                fom = LocalDate.now().minusMonths(1L),
                tom = LocalDate.now().plusMonths(3L),
                sats = 100.0
            )
        ))


        behandleTrekkService.behandleTrekk(pid,0, SatsType.KRONER)

        verify(exactly = 1) { trekkClientMock.opphorAndreTrekk(eq(pid), any())}
        verify(exactly = 0) { trekkClientMock.opprettAndreTrekk(eq(pid), any()) }

    }

    @Test
    fun `Opphor trekk dersom for usortert med ett løpende trekk og ikke gjøre noe med ett lukket via behandle trekk`() {

        val trekkvedtakId = 1L
        val trekkvedtakId2 = 2L

        every { trekkClientMock.finnTrekkListe(pid, any()) } returns listOf(
            lagTrekkInfo(
                trekkvedtakId = trekkvedtakId2,
                sats = BigDecimal.valueOf(20),
                ansvarligEnhetId = "NAV Enhet",
                fom = LocalDate.now().minusMonths(5L),
                tom = LocalDate.now().minusMonths(3L)
            ),
            lagTrekkInfo(
                trekkvedtakId = trekkvedtakId,
                sats = BigDecimal.valueOf(20),
                ansvarligEnhetId = "NAV Enhet",
                fom = LocalDate.now().minusMonths(1L),
                tom = null
            )
        )
        every { trekkClientMock.opphorAndreTrekk(eq(pid), any()) } returns Unit
        every { geografiskLokasjonServiceMock.hentNavEnhet(pid) } returns "NAV Enhet"
        every { trekkClientMock.hentSkattOgTrekk(pid, trekkvedtakId) } returns lagHentSkattOgTrekkRespons(trekkvedtakId, listOf(
            lagSatsperiode(
                fom = LocalDate.now().minusMonths(1L),
                tom = LocalDate.now().plusMonths(3L),
                sats = 100.0
            )
        ))

        behandleTrekkService.behandleTrekk(pid,0, SatsType.KRONER)

        verify(exactly = 1) { trekkClientMock.opphorAndreTrekk(eq(pid), any())}
        verify(exactly = 0) { trekkClientMock.opprettAndreTrekk(eq(pid), any()) }
    }

    @Test
    fun `Oppdater trekk dersom for usortert med ett løpende trekk og ikke gjøre noe med ett lukket via behandle trekk`() {

        val trekkvedtakId = 1L
        val trekkvedtakId2 = 2L

        every { trekkClientMock.finnTrekkListe(pid, any()) } returns listOf(
            lagTrekkInfo(
                trekkvedtakId = trekkvedtakId2,
                sats = BigDecimal.valueOf(20),
                ansvarligEnhetId = "NAV Enhet",
                fom = LocalDate.now().minusMonths(5L),
                tom = LocalDate.now().minusMonths(3L)
            ),
            lagTrekkInfo(
                trekkvedtakId = trekkvedtakId,
                sats = BigDecimal.valueOf(20),
                ansvarligEnhetId = "NAV Enhet",
                fom = LocalDate.now().minusMonths(1L),
                tom = null
            )
        )
        every { trekkClientMock.opphorAndreTrekk(eq(pid), any()) } returns Unit
        every { trekkClientMock.opprettAndreTrekk(eq(pid), any()) } returns OpprettAndreTrekkResponse(3L)
        every { geografiskLokasjonServiceMock.hentNavEnhet(pid) } returns "NAV Enhet"
        every { trekkClientMock.hentSkattOgTrekk(pid, trekkvedtakId2) } returns lagHentSkattOgTrekkRespons(trekkvedtakId, listOf(
            lagSatsperiode(
                fom = LocalDate.now().minusMonths(1L),
                tom = LocalDate.now().minusMonths(2L),
                sats = 100.0
            )
        ))
        every { trekkClientMock.hentSkattOgTrekk(pid, trekkvedtakId) } returns lagHentSkattOgTrekkRespons(trekkvedtakId, listOf(
            lagSatsperiode(
                fom = LocalDate.now().minusMonths(1L),
                tom = LocalDate.now().plusMonths(3L),
                sats = 100.0
            )
        ))

        behandleTrekkService.behandleTrekk(pid,30, SatsType.PROSENT)

        verify(exactly = 1) { trekkClientMock.opphorAndreTrekk(eq(pid), any()) }
        verify(exactly = 1) { trekkClientMock.opprettAndreTrekk(eq(pid), any()) }
    }

    @Test
    fun `Opprett nytt frivillig skattetrekk for prosent`() {

        every { geografiskLokasjonServiceMock.hentNavEnhet(pid) } returns "NAV Enhet"
        every { trekkClientMock.opprettAndreTrekk(eq(pid), any()) } returns OpprettAndreTrekkResponse(1L)

        val trekkVedtakId = behandleTrekkService.opprettTrekk(pid, 20, SatsType.PROSENT)

        verify(exactly = 1) { trekkClientMock.opprettAndreTrekk(eq(pid), any()) }
        verify(exactly = 0) { trekkClientMock.opphorAndreTrekk(eq(pid), any()) }

        assertEquals(1L,trekkVedtakId)
    }

    @Test
    fun `Opprett nytt frivillig skattetrekk for nok`() {

        every { geografiskLokasjonServiceMock.hentNavEnhet(pid) } returns "NAV Enhet"
        every { trekkClientMock.opprettAndreTrekk(eq(pid), any()) } returns OpprettAndreTrekkResponse(1L)

        val trekkVedtakId = behandleTrekkService.opprettTrekk(pid, 20, SatsType.KRONER)

        verify(exactly = 1) { trekkClientMock.opprettAndreTrekk(eq(pid), any()) }

        assertEquals(1L,trekkVedtakId)
    }

    @Test
    fun `Ikke opprett nytt frivillig skattetrekk dersom tilleggstrekk er 0`() {

        val trekkvedtakId = behandleTrekkService.opprettTrekk(pid, 0, SatsType.KRONER)

        verify(exactly = 0) { trekkClientMock.opprettAndreTrekk(eq(pid), any()) }
        verify(exactly = 0) { geografiskLokasjonServiceMock.hentNavEnhet(pid) }

        assertNull(trekkvedtakId)
    }

    @Test
    fun `Ikke opphør noe dersom det bare er tomme satsperioder`() {
        val trekkVedtakId = 1L

        every { geografiskLokasjonServiceMock.hentNavEnhet(pid) } returns "NAV Enhet"
        every { trekkClientMock.hentSkattOgTrekk(pid, trekkVedtakId) } returns lagHentSkattOgTrekkRespons(trekkVedtakId, emptyList())

        behandleTrekkService.opphoerTrekk(pid, trekkVedtakId)

        verify(exactly = 0) { trekkClientMock.opphorAndreTrekk(eq(pid), any()) }
    }

    @Test
    fun `Opphør trekk med løpende satsperiode`() {
        val trekkVedtakId = 1L

        every { geografiskLokasjonServiceMock.hentNavEnhet(pid) } returns "NAV Enhet"
        every { trekkClientMock.opphorAndreTrekk(eq(pid), any()) } returns Unit

        every { trekkClientMock.hentSkattOgTrekk(pid, trekkVedtakId) } returns lagHentSkattOgTrekkRespons(trekkVedtakId,
            listOf(lagSatsperiode(
                fom = LocalDate.now().minusMonths(1L),
                tom = LocalDate.now().plusMonths(3L),
                sats = 100.0
            ))
        )

        behandleTrekkService.opphoerTrekk(pid, trekkVedtakId)

        verify(exactly = 1) { trekkClientMock.opphorAndreTrekk(eq(pid), any()) }
    }

    @Test
    fun `Opphør trekk med fremtidig satsperiode`() {
        val trekkVedtakId = 1L

        every { geografiskLokasjonServiceMock.hentNavEnhet(pid) } returns "NAV Enhet"
        every { trekkClientMock.opphorAndreTrekk(eq(pid), any()) } returns Unit

        every { trekkClientMock.hentSkattOgTrekk(pid, trekkVedtakId) } returns lagHentSkattOgTrekkRespons(trekkVedtakId,
            listOf(lagSatsperiode(
                fom = LocalDate.now().plusMonths(1L),
                tom = LocalDate.now().plusMonths(3L),
                sats = 100.0
            ))
        )

        behandleTrekkService.opphoerTrekk(pid, trekkVedtakId)

        verify(exactly = 1) { trekkClientMock.opphorAndreTrekk(eq(pid), any()) }
    }

    @Test
    fun `Opphør trekk med både løpende og fremtidig satsperiode`() {
        val trekkVedtakId = 1L

        every { geografiskLokasjonServiceMock.hentNavEnhet(pid) } returns "NAV Enhet"
        every { trekkClientMock.opphorAndreTrekk(eq(pid), any()) } returns Unit

        every { trekkClientMock.hentSkattOgTrekk(pid, trekkVedtakId) } returns lagHentSkattOgTrekkRespons(trekkVedtakId,
            listOf(
                lagSatsperiode(
                    fom = LocalDate.now().minusMonths(1L),
                    tom = LocalDate.now().plusMonths(3L),
                    sats = 100.0),
                lagSatsperiode(
                    fom = LocalDate.now().plusMonths(1L),
                    tom = LocalDate.now().plusMonths(3L),
                    sats = 100.0
                ))
        )

        behandleTrekkService.opphoerTrekk(pid, trekkVedtakId)

        verify(exactly = 1) {
            trekkClientMock.opphorAndreTrekk(
                eq(pid),
                any()
            )
        }
    }

    @Test
    fun `Skal ikke opphøre noe dersom gammel satsperiode`() {
        val trekkVedtakId = 1L

        every { geografiskLokasjonServiceMock.hentNavEnhet(pid) } returns "NAV Enhet"

        every { trekkClientMock.hentSkattOgTrekk(pid, trekkVedtakId) } returns lagHentSkattOgTrekkRespons(trekkVedtakId,
            listOf(
                lagSatsperiode(
                    fom = LocalDate.now().minusMonths(10L),
                    tom = LocalDate.now().minusMonths(2L),
                    sats = 100.0))
        )

        behandleTrekkService.opphoerTrekk(pid, trekkVedtakId)

        verify(exactly = 0) {
            trekkClientMock.opphorAndreTrekk(
                eq(pid),
                any()
            )
        }
    }

    @Test
    fun `Skal opphøre dersom trekk inneholder satsperioder med mix av gamle, løpende og fremtidige`() {
        val trekkVedtakId = 1L

        every { geografiskLokasjonServiceMock.hentNavEnhet(pid) } returns "NAV Enhet"
        every { trekkClientMock.opphorAndreTrekk(eq(pid), any()) } returns Unit

        every { trekkClientMock.hentSkattOgTrekk(pid, trekkVedtakId) } returns lagHentSkattOgTrekkRespons(trekkVedtakId,
            listOf(
                lagSatsperiode(
                    fom = LocalDate.now().minusMonths(10L),
                    tom = LocalDate.now().minusMonths(2L),
                    sats = 100.0),
                lagSatsperiode(
                    fom = LocalDate.now().minusMonths(1L),
                    tom = LocalDate.now().plusMonths(3L),
                    sats = 100.0),
                lagSatsperiode(
                    fom = LocalDate.now().plusMonths(4L),
                    tom = LocalDate.now().minusMonths(10L),
                    sats = 100.0)
                )
        )

        behandleTrekkService.opphoerTrekk(pid, trekkVedtakId)

        verify(exactly = 1) {
            trekkClientMock.opphorAndreTrekk(
                eq(pid),
                any()
            )
        }
    }

    @Test
    fun `Oppdater skattetrekk`() {
        val trekkVedtakId = 1L
        val tilleggstrekk = 20

        val trekkListe = listOf(
            lagTrekkInfo(
                trekkvedtakId = trekkVedtakId,
                sats = BigDecimal.valueOf(20),
                ansvarligEnhetId = "NAV Enhet",
                fom = LocalDate.now().minusMonths(1L),
                tom = null
            )
        )

        every { geografiskLokasjonServiceMock.hentNavEnhet(pid) } returns "NAV Enhet"
        every { trekkClientMock.opphorAndreTrekk(eq(pid), any()) } returns Unit
        every { trekkClientMock.opprettAndreTrekk(eq(pid), any()) } returns OpprettAndreTrekkResponse(1L)

        every { trekkClientMock.hentSkattOgTrekk(pid, trekkVedtakId) } returns lagHentSkattOgTrekkRespons(trekkVedtakId,
            listOf(lagSatsperiode(
                fom = LocalDate.now().minusMonths(1L),
                tom = LocalDate.now().plusMonths(3L),
                sats = 100.0
            ))
        )

        every { trekkClientMock.finnTrekkListe(pid, any()) } returns trekkListe

        behandleTrekkService.oppdaterTrekk(pid, trekkListe, tilleggstrekk, SatsType.KRONER)

        verify(exactly = 1) {
            trekkClientMock.opphorAndreTrekk(
                eq(pid),
                any()
            )
        }

        verify(exactly = 1) {
            trekkClientMock.opprettAndreTrekk(
                eq(pid),
                any()
            )
        }
    }

    private fun lagSatsperiode(fom: LocalDate, tom: LocalDate, sats: Double): Satsperiode {
        return Satsperiode(fom, tom, BigDecimal.valueOf(sats), erFeilregistrert = false)
    }

    private fun lagHentSkattOgTrekkRespons(andreTrekkVedtakId: Long, satsperiodeListe: List<Satsperiode>) = HentSkattOgTrekkResponse(
        skattetrekk = null,
        andreTrekk = AndreTrekkResponse(
            trekkvedtakId = andreTrekkVedtakId,
            debitor = null,
            trekktype  = null,
            trekkstatus = null,
            kreditor = null,
            kreditorAvdelingsnr = null,
            kreditorRef = null,
            kreditorKid = null,
            tssEksternId = null,
            prioritet = null,
            prioritetFom = null,
            trekkalternativ = null,
            belopSaldotrekk = null,
            belopTrukket = null,
            datoOppfolging = null,
            gyldigTom = null,
            ansvarligEnhetId = null,
            sporing = null,
            fagomradeListe = listOf(Fagomrade(
                trekkgruppeKode = "PENA",
                fagomradeKode = "PENA",
                erFeilregistrert = false
            )),
            satsperiodeListe = satsperiodeListe
        )
    )

    private fun lagTrekkInfo(trekkvedtakId: Long, sats: BigDecimal, ansvarligEnhetId: String?, fom: LocalDate, tom: LocalDate?) = TrekkInfo(
        trekkvedtakId = trekkvedtakId,
        debitor = null,
        trekktype = null,
        trekkperiodeFom = fom,
        trekkperiodeTom = tom,
        trekkstatus = null,
        kreditor = null,
        kreditorRef = null,
        tssEksternId = null,
        trekkalternativ = null,
        sats = sats,
        belopSaldotrekk = null,
        belopTrukket = null,
        ansvarligEnhetId = ansvarligEnhetId,
    )
}