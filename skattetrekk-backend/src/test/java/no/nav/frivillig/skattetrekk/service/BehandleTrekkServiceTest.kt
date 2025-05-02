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
    fun `opprett nytt frivillig skattetrekk for prosent`() {

        every { geografiskLokasjonServiceMock.hentNavEnhet(pid) } returns "NAV Enhet"
        every { trekkClientMock.opprettAndreTrekk(eq(pid), any()) } returns OpprettAndreTrekkResponse(1L)

        val trekkVedtakId = behandleTrekkService.opprettTrekk(pid, 20, SatsType.PROSENT)

        verify(exactly = 1) {
            trekkClientMock.opprettAndreTrekk(
                eq(pid),
                any()
            )
        }

        assertEquals(1L,trekkVedtakId)
    }

    @Test
    fun `opprett nytt frivillig skattetrekk for nok`() {

        every { geografiskLokasjonServiceMock.hentNavEnhet(pid) } returns "NAV Enhet"
        every { trekkClientMock.opprettAndreTrekk(eq(pid), any()) } returns OpprettAndreTrekkResponse(1L)

        val trekkVedtakId = behandleTrekkService.opprettTrekk(pid, 20, SatsType.KRONER)

        verify(exactly = 1) {
            trekkClientMock.opprettAndreTrekk(
                eq(pid),
                any()
            )
        }

        assertEquals(1L,trekkVedtakId)
    }

    @Test
    fun `ikke opprett nytt frivillig skattetrekk dersom tilleggstrekk er 0`() {

        every { trekkClientMock.opprettAndreTrekk(eq(pid), any()) } returns OpprettAndreTrekkResponse(1L)

        behandleTrekkService.opprettTrekk(pid, 0, SatsType.KRONER)

        verify(exactly = 0) {
            trekkClientMock.opprettAndreTrekk(
                eq(pid),
                any()
            )
        }

        verify(exactly = 0) {
            geografiskLokasjonServiceMock.hentNavEnhet(pid)
        }

        assertNull(0)
    }

    private fun lagFinnTrekkListe():List<TrekkInfo> = listOf(
            TrekkInfo(
                trekkvedtakId = null,
                debitor = null,
                trekktype = null,
                trekkperiodeFom = null,
                trekkperiodeTom = null,
                trekkstatus = null,
                kreditor = null,
                kreditorRef = null,
                tssEksternId = null,
                trekkalternativ = null,
                sats = null,
                belopSaldotrekk = null,
                belopTrukket = null,
                ansvarligEnhetId = null,
            )
        )


    private fun lagSatsperiode(fom: LocalDate, tom: LocalDate, sats: Double): Satsperiode {
        return Satsperiode(fom, tom, BigDecimal.valueOf(sats), erFeilregistrert = false)
    }

    private fun lagHentSkattOgTrekkRespons(
        andreTrekkVedtakId: Long,
        satsperiodeListe: List<Satsperiode>
        ) = HentSkattOgTrekkResponse(
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
}