package no.nav.frivillig.skattetrekk.service

import io.mockk.every
import no.nav.frivillig.skattetrekk.client.trekk.TrekkClient
import no.nav.frivillig.skattetrekk.client.trekk.api.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.math.BigDecimal
import java.time.LocalDate

@Disabled
class BehandleTrekkServiceTest {

    private val pid = "12345678910"
    private val trekkClientMock = Mockito.mock(TrekkClient::class.java)
    private val geografiskLokasjonServiceMock = Mockito.mock(GeografiskLokasjonService::class.java)
    private val behandleTrekkService = BehandleTrekkService(trekkClientMock, geografiskLokasjonServiceMock)

    @Test
    fun `oppdatere frivillig skattetrekk for prosent`() {
        
        every { trekkClientMock.finnTrekkListe(pid, TrekkTypeCode.FRIS) } returns emptyList()
        behandleTrekkService.opprettTrekk(pid, 20, SatsType.PROSENT)

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