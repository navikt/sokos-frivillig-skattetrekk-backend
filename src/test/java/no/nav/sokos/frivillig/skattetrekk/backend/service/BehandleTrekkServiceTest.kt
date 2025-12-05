package no.nav.sokos.frivillig.skattetrekk.backend.service

import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.TrekkClient
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.AndreTrekkResponse
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.Bruker
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.FagomradeResponse
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.HentSkattOgTrekkResponse
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.OpprettAndreTrekkResponse
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.SatsType
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.Satsperiode
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.TrekkInfo
import no.nav.sokos.frivillig.skattetrekk.backend.client.trekk.api.Trekktype

class BehandleTrekkServiceTest {
    private val pid = "12345678910"
    private val trekkClientMock = mockk<TrekkClient>()
    private val behandleTrekkService =
        BehandleTrekkService(
            trekkClientMock,
        )

    @Test
    fun `Opphor trekk dersom ett løpende trekk via behandle trekk`() {
        val trekkvedtakId = 1L

        every { trekkClientMock.finnTrekkListe(pid, any()) } returns
            listOf(
                lagTrekkInfo(
                    trekkvedtakId = trekkvedtakId,
                    sats = BigDecimal.valueOf(20),
                    ansvarligEnhetId = "NAV Enhet",
                    fom = LocalDate.parse("2025-05-01"),
                    tom = LocalDate.now().plusMonths(3).with(TemporalAdjusters.lastDayOfMonth()),
                ),
            )
        every { trekkClientMock.opphorAndreTrekk(eq(pid), any()) } returns Unit
        every { trekkClientMock.hentSkattOgTrekk(pid, trekkvedtakId) } returns
            lagHentSkattOgTrekkRespons(
                trekkvedtakId,
                listOf(
                    lagSatsperiode(
                        fom = LocalDate.parse("2025-05-01"),
                        tom = LocalDate.parse("2025-12-31"),
                        sats = 100.0,
                    ),
                ),
            )

        behandleTrekkService.behandleTrekk(pid, 0, SatsType.KRONER)

        verify(exactly = 1) {
            trekkClientMock.opphorAndreTrekk(
                eq(pid),
                any(),
            )
        }
    }

    @Test
    fun `Opprett trekk dersom tom trekkliste via behandle trekk`() {
        val trekkvedtakId = 1L

        every { trekkClientMock.finnTrekkListe(pid, any()) } returns emptyList()
        every { trekkClientMock.opprettAndreTrekk(eq(pid), any()) } returns OpprettAndreTrekkResponse(trekkvedtakId)
        every { trekkClientMock.hentSkattOgTrekk(pid, trekkvedtakId) } returns
            lagHentSkattOgTrekkRespons(
                trekkvedtakId,
                listOf(
                    lagSatsperiode(
                        fom = LocalDate.now().minusMonths(1L),
                        tom = LocalDate.now().plusMonths(3L),
                        sats = 100.0,
                    ),
                ),
            )

        behandleTrekkService.behandleTrekk(pid, 40, SatsType.PROSENT)

        verify(exactly = 0) { trekkClientMock.opphorAndreTrekk(eq(pid), any()) }
        verify(exactly = 1) { trekkClientMock.opprettAndreTrekk(eq(pid), any()) }
    }

    @Test
    fun `Opphor trekk dersom ett løpende trekk og ikke gjøre noe med ett lukket via behandle trekk`() {
        val trekkvedtakId = 1L
        val trekkvedtakId2 = 2L

        every { trekkClientMock.finnTrekkListe(pid, any()) } returns
            listOf(
                lagTrekkInfo(
                    trekkvedtakId = trekkvedtakId,
                    sats = BigDecimal.valueOf(20),
                    ansvarligEnhetId = "NAV Enhet",
                    fom = LocalDate.parse("2025-05-01"),
                    tom = LocalDate.parse("2025-05-30"),
                ),
                lagTrekkInfo(
                    trekkvedtakId = trekkvedtakId2,
                    sats = BigDecimal.valueOf(20),
                    ansvarligEnhetId = "NAV Enhet",
                    fom = LocalDate.parse("2025-06-01"),
                    tom = LocalDate.now().plusMonths(3).with(TemporalAdjusters.lastDayOfMonth()),
                ),
            )
        every { trekkClientMock.opphorAndreTrekk(eq(pid), any()) } returns Unit
        every { trekkClientMock.hentSkattOgTrekk(pid, any()) } returns
            lagHentSkattOgTrekkRespons(
                trekkvedtakId,
                listOf(
                    lagSatsperiode(
                        fom = LocalDate.parse("2025-06-01"),
                        tom = LocalDate.parse("2025-12-31"),
                        sats = 100.0,
                    ),
                ),
            )

        behandleTrekkService.behandleTrekk(pid, 0, SatsType.KRONER)

        verify(exactly = 1) { trekkClientMock.opphorAndreTrekk(eq(pid), any()) }
        verify(exactly = 0) { trekkClientMock.opprettAndreTrekk(eq(pid), any()) }
    }

    @Test
    fun `Opphor trekk dersom for usortert med ett løpende trekk og ikke gjøre noe med ett lukket via behandle trekk`() {
        val trekkvedtakId = 1L
        val trekkvedtakId2 = 2L

        every { trekkClientMock.finnTrekkListe(pid, any()) } returns
            listOf(
                lagTrekkInfo(
                    trekkvedtakId = trekkvedtakId2,
                    sats = BigDecimal.valueOf(20),
                    ansvarligEnhetId = "NAV Enhet",
                    fom = LocalDate.parse("2025-01-01"),
                    tom = LocalDate.parse("2025-03-01"),
                ),
                lagTrekkInfo(
                    trekkvedtakId = trekkvedtakId,
                    sats = BigDecimal.valueOf(20),
                    ansvarligEnhetId = "NAV Enhet",
                    fom = LocalDate.parse("2025-05-01"),
                    tom = LocalDate.now().plusMonths(3).with(TemporalAdjusters.lastDayOfMonth()),
                ),
            )
        every { trekkClientMock.opphorAndreTrekk(eq(pid), any()) } returns Unit
        every { trekkClientMock.hentSkattOgTrekk(pid, any()) } returns
            lagHentSkattOgTrekkRespons(
                trekkvedtakId,
                listOf(
                    lagSatsperiode(
                        fom = LocalDate.parse("2025-01-01"),
                        tom = LocalDate.parse("2025-03-01"),
                        sats = 100.0,
                    ),
                ),
            )

        behandleTrekkService.behandleTrekk(pid, 0, SatsType.KRONER)

        verify(exactly = 1) { trekkClientMock.opphorAndreTrekk(eq(pid), any()) }
        verify(exactly = 0) { trekkClientMock.opprettAndreTrekk(eq(pid), any()) }
    }

    @Test
    fun `Oppdater trekk dersom for usortert med ett løpende trekk og ikke gjøre noe med ett lukket via behandle trekk`() {
        val trekkvedtakId = 1L
        val trekkvedtakId2 = 2L

        every { trekkClientMock.finnTrekkListe(pid, any()) } returns
            listOf(
                lagTrekkInfo(
                    trekkvedtakId = trekkvedtakId2,
                    sats = BigDecimal.valueOf(20),
                    ansvarligEnhetId = "NAV Enhet",
                    fom = LocalDate.now().minusMonths(5L),
                    tom = LocalDate.now().minusMonths(3L),
                ),
                lagTrekkInfo(
                    trekkvedtakId = trekkvedtakId,
                    sats = BigDecimal.valueOf(20),
                    ansvarligEnhetId = "NAV Enhet",
                    fom = LocalDate.now().minusMonths(1L),
                    tom = null,
                ),
            )
        every { trekkClientMock.opphorAndreTrekk(eq(pid), any()) } returns Unit
        every { trekkClientMock.opprettAndreTrekk(eq(pid), any()) } returns OpprettAndreTrekkResponse(3L)
        every { trekkClientMock.hentSkattOgTrekk(pid, trekkvedtakId2) } returns
            lagHentSkattOgTrekkRespons(
                trekkvedtakId,
                listOf(
                    lagSatsperiode(
                        fom = LocalDate.now().minusMonths(1L),
                        tom = LocalDate.now().minusMonths(2L),
                        sats = 100.0,
                    ),
                ),
            )
        every { trekkClientMock.hentSkattOgTrekk(pid, trekkvedtakId) } returns
            lagHentSkattOgTrekkRespons(
                trekkvedtakId,
                listOf(
                    lagSatsperiode(
                        fom = LocalDate.now().minusMonths(1L),
                        tom = LocalDate.now().plusMonths(3L),
                        sats = 100.0,
                    ),
                ),
            )

        behandleTrekkService.behandleTrekk(pid, 30, SatsType.PROSENT)

        verify(exactly = 0) { trekkClientMock.opphorAndreTrekk(eq(pid), any()) }
        verify(exactly = 1) { trekkClientMock.opprettAndreTrekk(eq(pid), any()) }
    }

    @Test
    fun `Opprett nytt frivillig skattetrekk for prosent`() {
        every { trekkClientMock.opprettAndreTrekk(eq(pid), any()) } returns OpprettAndreTrekkResponse(1L)

        val trekkVedtakId = behandleTrekkService.opprettTrekk(pid, 20, SatsType.PROSENT, LocalDate.parse("2025-01-01"))

        verify(exactly = 1) { trekkClientMock.opprettAndreTrekk(eq(pid), any()) }
        verify(exactly = 0) { trekkClientMock.opphorAndreTrekk(eq(pid), any()) }

        assertEquals(1L, trekkVedtakId)
    }

    @Test
    fun `Opprett nytt frivillig skattetrekk for nok`() {
        every { trekkClientMock.opprettAndreTrekk(eq(pid), any()) } returns OpprettAndreTrekkResponse(1L)

        val trekkVedtakId = behandleTrekkService.opprettTrekk(pid, 20, SatsType.KRONER, LocalDate.parse("2025-01-01"))

        verify(exactly = 1) { trekkClientMock.opprettAndreTrekk(eq(pid), any()) }

        assertEquals(1L, trekkVedtakId)
    }

    @Test
    fun `Ikke opprett nytt frivillig skattetrekk dersom tilleggstrekk er 0`() {
        val trekkvedtakId = behandleTrekkService.opprettTrekk(pid, 0, SatsType.KRONER, LocalDate.parse("2025-01-01"))

        verify(exactly = 0) { trekkClientMock.opprettAndreTrekk(eq(pid), any()) }

        assertNull(trekkvedtakId)
    }

    @Test
    fun `Ikke opphør noe dersom det bare er tomme satsperioder`() {
        val trekkVedtakId = 1L

        every { trekkClientMock.hentSkattOgTrekk(pid, trekkVedtakId) } returns lagHentSkattOgTrekkRespons(trekkVedtakId, emptyList())

        verify(exactly = 0) { trekkClientMock.opphorAndreTrekk(eq(pid), any()) }
    }

    @Test
    fun `Opphør trekk med løpende satsperiode`() {
        val trekkVedtakId = 1L

        every { trekkClientMock.opphorAndreTrekk(eq(pid), any()) } returns Unit

        every { trekkClientMock.hentSkattOgTrekk(pid, trekkVedtakId) } returns
            lagHentSkattOgTrekkRespons(
                trekkVedtakId,
                listOf(
                    lagSatsperiode(
                        fom = LocalDate.now().minusMonths(1L),
                        tom = LocalDate.now().plusMonths(3L),
                        sats = 100.0,
                    ),
                ),
            )

        behandleTrekkService.opphoerTrekk(pid, trekkVedtakId)

        verify(exactly = 1) { trekkClientMock.opphorAndreTrekk(eq(pid), any()) }
    }

    @Test
    fun `Opphør trekk med fremtidig satsperiode`() {
        val trekkVedtakId = 1L

        every { trekkClientMock.opphorAndreTrekk(eq(pid), any()) } returns Unit

        every { trekkClientMock.hentSkattOgTrekk(pid, trekkVedtakId) } returns
            lagHentSkattOgTrekkRespons(
                trekkVedtakId,
                listOf(
                    lagSatsperiode(
                        fom = LocalDate.now().plusMonths(1L),
                        tom = LocalDate.now().plusMonths(3L),
                        sats = 100.0,
                    ),
                ),
            )

        behandleTrekkService.opphoerTrekk(pid, trekkVedtakId)

        verify(exactly = 1) { trekkClientMock.opphorAndreTrekk(eq(pid), any()) }
    }

    @Test
    fun `Opphør trekk med både løpende og fremtidig satsperiode`() {
        val trekkVedtakId = 1L

        every { trekkClientMock.opphorAndreTrekk(eq(pid), any()) } returns Unit

        every { trekkClientMock.hentSkattOgTrekk(pid, trekkVedtakId) } returns
            lagHentSkattOgTrekkRespons(
                trekkVedtakId,
                listOf(
                    lagSatsperiode(
                        fom = LocalDate.now().minusMonths(1L),
                        tom = LocalDate.now().plusMonths(3L),
                        sats = 100.0,
                    ),
                    lagSatsperiode(
                        fom = LocalDate.now().plusMonths(1L),
                        tom = LocalDate.now().plusMonths(3L),
                        sats = 100.0,
                    ),
                ),
            )

        behandleTrekkService.opphoerTrekk(pid, trekkVedtakId)

        verify(exactly = 1) {
            trekkClientMock.opphorAndreTrekk(
                eq(pid),
                any(),
            )
        }
    }

    @Test
    fun `Skal ikke opphøre noe dersom gammel satsperiode`() {
        val trekkVedtakId = 1L

        every { trekkClientMock.hentSkattOgTrekk(pid, trekkVedtakId) } returns
            lagHentSkattOgTrekkRespons(
                trekkVedtakId,
                listOf(
                    lagSatsperiode(
                        fom = LocalDate.now().minusMonths(10L),
                        tom = LocalDate.now().minusMonths(2L),
                        sats = 100.0,
                    ),
                ),
            )

        verify(exactly = 0) {
            trekkClientMock.opphorAndreTrekk(
                eq(pid),
                any(),
            )
        }
    }

    @Test
    fun `Skal opphøre dersom trekk inneholder satsperioder med mix av gamle, løpende og fremtidige`() {
        val trekkVedtakId = 1L

        every { trekkClientMock.opphorAndreTrekk(eq(pid), any()) } returns Unit

        every { trekkClientMock.hentSkattOgTrekk(pid, trekkVedtakId) } returns
            lagHentSkattOgTrekkRespons(
                trekkVedtakId,
                listOf(
                    lagSatsperiode(
                        fom = LocalDate.now().minusMonths(10L),
                        tom = LocalDate.now().minusMonths(2L),
                        sats = 100.0,
                    ),
                    lagSatsperiode(
                        fom = LocalDate.now().minusMonths(1L),
                        tom = LocalDate.now().plusMonths(3L),
                        sats = 100.0,
                    ),
                    lagSatsperiode(
                        fom = LocalDate.now().plusMonths(4L),
                        tom = LocalDate.now().minusMonths(10L),
                        sats = 100.0,
                    ),
                ),
            )

        behandleTrekkService.opphoerTrekk(pid, trekkVedtakId)

        verify(exactly = 1) {
            trekkClientMock.opphorAndreTrekk(
                eq(pid),
                any(),
            )
        }
    }

    @Test
    fun `Oppdater skattetrekk`() {
        val trekkVedtakId = 1L
        val tilleggstrekk = 20

        val trekkListe =
            listOf(
                lagTrekkInfo(
                    trekkvedtakId = trekkVedtakId,
                    sats = BigDecimal.valueOf(20),
                    ansvarligEnhetId = "NAV Enhet",
                    fom = LocalDate.now().minusMonths(1L),
                    tom = null,
                ),
            )

        every { trekkClientMock.opphorAndreTrekk(eq(pid), any()) } returns Unit
        every { trekkClientMock.opprettAndreTrekk(eq(pid), any()) } returns OpprettAndreTrekkResponse(1L)
        every { trekkClientMock.oppdaterAndreTrekk(eq(pid), any()) } returns Unit

        every { trekkClientMock.hentSkattOgTrekk(pid, trekkVedtakId) } returns
            lagHentSkattOgTrekkRespons(
                trekkVedtakId,
                listOf(
                    lagSatsperiode(
                        fom = LocalDate.now().minusMonths(1L),
                        tom = LocalDate.now().plusMonths(3L),
                        sats = 100.0,
                    ),
                ),
            )

        every { trekkClientMock.finnTrekkListe(pid, any()) } returns trekkListe

        behandleTrekkService.oppdaterTrekk(pid, trekkVedtakId, tilleggstrekk, SatsType.KRONER)

        verify(exactly = 1) {
            trekkClientMock.oppdaterAndreTrekk(
                eq(pid),
                any(),
            )
        }
    }

    @Test
    fun `Skal lukke den siste åpne eksisterende satsperioden med dagen før starten på ny satsperiode`() {
        val eksisterendeListe =
            listOf(
                lagSatsperiode(fom = LocalDate.parse("2025-01-01"), tom = LocalDate.parse("2025-02-28"), sats = 1.0),
                lagSatsperiode(fom = LocalDate.parse("2025-03-01"), LocalDate.parse("2025-03-31"), sats = 2.0),
                lagSatsperiode(fom = LocalDate.parse("2025-04-01"), LocalDate.parse("2025-12-31"), sats = 3.0),
            )

        val nySatsperiode =
            lagSatsperiode(
                fom = LocalDate.parse("2025-05-01"),
                tom = LocalDate.parse("2025-12-31"),
                sats = 200.0,
            )

        val oppdaterteSatsperioderListe = behandleTrekkService.oppdaterSatsperioder(LocalDate.now(), eksisterendeListe, nySatsperiode)

        assertEquals(4, oppdaterteSatsperioderListe.size)
        assertEquals(LocalDate.parse("2025-04-30"), oppdaterteSatsperioderListe[2].tom)
        assertEquals(nySatsperiode, oppdaterteSatsperioderListe.last())
    }

    @Test
    fun `Skal lukke den siste åpne eksisterende satsperioden med null tom dato med dagen før starten på ny satsperiode`() {
        val eksisterendeListe =
            listOf(
                lagSatsperiode(fom = LocalDate.parse("2025-01-01"), tom = LocalDate.parse("2025-02-28"), sats = 1.0),
                lagSatsperiode(fom = LocalDate.parse("2025-03-01"), LocalDate.parse("2025-03-31"), sats = 2.0),
                lagSatsperiode(fom = LocalDate.parse("2025-04-01"), null, sats = 3.0),
            )

        val nySatsperiode =
            lagSatsperiode(
                fom = LocalDate.parse("2025-05-01"),
                tom = LocalDate.parse("2025-12-31"),
                sats = 200.0,
            )

        val oppdaterteSatsperioderListe = behandleTrekkService.oppdaterSatsperioder(LocalDate.now(), eksisterendeListe, nySatsperiode)

        assertEquals(4, oppdaterteSatsperioderListe.size)
        assertEquals(LocalDate.parse("2025-04-30"), oppdaterteSatsperioderListe[2].tom)
        assertEquals(nySatsperiode, oppdaterteSatsperioderListe.last())
    }

    @Test
    fun `Skal lukke alle åpne eksisterende satsperioder med dagen før starten på ny satsperiode`() {
        val eksisterendeListe =
            listOf(
                lagSatsperiode(fom = LocalDate.parse("2025-01-01"), tom = LocalDate.parse("2025-02-28"), sats = 1.0),
                lagSatsperiode(fom = LocalDate.parse("2025-03-01"), LocalDate.parse("2025-12-31"), sats = 2.0),
                lagSatsperiode(fom = LocalDate.parse("2025-04-01"), LocalDate.parse("2025-12-31"), sats = 3.0),
            )

        val nySatsperiode =
            lagSatsperiode(
                fom = LocalDate.parse("2025-05-01"),
                tom = LocalDate.parse("2025-12-31"),
                sats = 200.0,
            )

        val oppdaterteSatsperioderListe = behandleTrekkService.oppdaterSatsperioder(LocalDate.now(), eksisterendeListe, nySatsperiode)

        assertEquals(4, oppdaterteSatsperioderListe.size)
        assertEquals(LocalDate.parse("2025-04-30"), oppdaterteSatsperioderListe[1].tom)
        assertEquals(LocalDate.parse("2025-04-30"), oppdaterteSatsperioderListe[2].tom)
        assertEquals(nySatsperiode, oppdaterteSatsperioderListe.last())
    }

    @Test
    fun `Skal fjerne fremtidig satsperiode med dagen før starten på ny satsperiode`() {
        val eksisterendeListe =
            listOf(
                lagSatsperiode(fom = LocalDate.parse("2025-01-01"), tom = LocalDate.parse("2025-02-28"), sats = 1.0),
                lagSatsperiode(fom = LocalDate.parse("2025-06-01"), LocalDate.parse("2025-12-31"), sats = 4.0),
            )

        val nySatsperiode =
            lagSatsperiode(
                fom = LocalDate.parse("2025-07-01"),
                tom = LocalDate.parse("2025-12-31"),
                sats = 3.0,
            )

        val oppdaterteSatsperioderListe =
            behandleTrekkService.oppdaterSatsperioder(
                LocalDate.parse("2025-06-16"),
                eksisterendeListe,
                nySatsperiode,
            )

        assertEquals(3, oppdaterteSatsperioderListe.size)
        assertEquals(LocalDate.parse("2025-06-01"), oppdaterteSatsperioderListe[1].fom)
        assertEquals(LocalDate.parse("2025-06-30"), oppdaterteSatsperioderListe[1].tom)
        assertEquals(LocalDate.parse("2025-07-01"), oppdaterteSatsperioderListe[2].fom)
        assertEquals(LocalDate.parse("2025-12-31"), oppdaterteSatsperioderListe[2].tom)
        assertEquals(nySatsperiode, oppdaterteSatsperioderListe.last())
    }

    private fun lagSatsperiode(
        fom: LocalDate,
        tom: LocalDate?,
        sats: Double,
    ): Satsperiode = Satsperiode(fom, tom, BigDecimal.valueOf(sats), erFeilregistrert = false)

    private fun lagHentSkattOgTrekkRespons(
        andreTrekkVedtakId: Long,
        satsperiodeListe: List<Satsperiode>,
    ) = HentSkattOgTrekkResponse(
        skattetrekk = null,
        andreTrekk =
            AndreTrekkResponse(
                trekkvedtakId = andreTrekkVedtakId,
                debitor = Bruker("123", "Bruker Navn"),
                trekktype = Trekktype("", ""),
                trekkstatus = null,
                kreditor = null,
                kreditorAvdelingsnr = null,
                kreditorRef = null,
                tssEksternId = null,
                prioritet = null,
                prioritetFom = null,
                trekkalternativ = null,
                belopSaldotrekk = null,
                belopTrukket = null,
                datoOppfolging = null,
                gyldigTom = null,
                ansvarligEnhetId = "4819",
                sporing = null,
                fagomradeListe =
                    listOf(
                        FagomradeResponse(
                            kode = "FRIS",
                            dekode = "FRIS",
                            sporing = null,
                        ),
                    ),
                satsperiodeListe = satsperiodeListe,
            ),
    )

    private fun lagTrekkInfo(
        trekkvedtakId: Long,
        sats: BigDecimal,
        ansvarligEnhetId: String?,
        fom: LocalDate,
        tom: LocalDate?,
    ) = TrekkInfo(
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
