package no.nav.frivillig.skattetrekk.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.frivillig.skattetrekk.client.trekk.TrekkClient
import no.nav.frivillig.skattetrekk.client.trekk.api.*
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class HentSkattOgTrekkServiceTest {

    private val trekkClientMock = mockk<TrekkClient>()
    private val hentSkattOgTrekkService = HentSkattOgTrekkService(trekkClientMock)

    @Test
    fun `tom respons når det ikke finnes hverken skattetrekk eller frivillig skattettrekk`() {
        val fnr = "12345678901"

        every { trekkClientMock.finnTrekkListe(fnr, TrekkTypeCode.FRIS) } returns emptyList()
        every { trekkClientMock.finnTrekkListe(fnr, TrekkTypeCode.FSKT) } returns emptyList()

        val result = hentSkattOgTrekkService.getSkattetrekk(fnr)
        verify(exactly = 1) { trekkClientMock.finnTrekkListe(fnr, TrekkTypeCode.FRIS) }
        verify(exactly = 1) { trekkClientMock.finnTrekkListe(fnr, TrekkTypeCode.FSKT) }
        verify(exactly = 0) { trekkClientMock.hentSkattOgTrekk(any(), any()) }
        assertNotNull(result)
        assertNotNull(result.skattetrekk)
        assertNull(result.framtidigTilleggstrekk)
        assertNull(result.tilleggstrekk)
    }

    private fun byggHentSkattOgTrekkResponse(
        skattetrekkTrekkVedtakId: Long?,
        frivilligSkattetrekkTrekkVedtakId: Long?,
    ) = HentSkattOgTrekkResponse(
            skattetrekk = Skattetrekk(
                trekkvedtakId = skattetrekkTrekkVedtakId,
                debitor = null,
                trekktype = null,
                trekkstatus = null,
                skattekommunenr = null,
                skattekommuneNavn = null,
                tabellnr = null,
                prosentsats = null,
                frikortFom = null,
                frikortTom = null,
                trekkperiodeFom = null,
                trekkperiodeTom = null,
                tabellIFaggruppe = null,
                sporing = null,
            ),
            andreTrekk = AndreTrekkResponse(
                trekkvedtakId = frivilligSkattetrekkTrekkVedtakId,
                debitor =  null,
                trekktype = null,
                trekkstatus = null,
                kreditor = null,
                kreditorAvdelingsnr = null,
                kreditorRef = null,
                kreditorKid = null,
                tssEksternId = null,
                trekkalternativ = null,
                prioritet = null,
                prioritetFom = null,
                belopSaldotrekk = null,
                belopTrukket = null,
                datoOppfolging = null,
                gyldigTom = null,
                ansvarligEnhetId = null,
                sporing = null,
                fagomradeListe = null,
                satsperiodeListe = null,
            )
        )

}