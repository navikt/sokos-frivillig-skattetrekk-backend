package no.nav.frivillig.skattetrekk.endpoint

import io.mockk.every
import no.nav.pensjon.selvbetjening.skattetrekk.endpoint.api.ForenkletSkattetrekk
import no.nav.pensjon.selvbetjening.skattetrekk.endpoint.api.FrivilligSkattetrekkInitResponse
import no.nav.pensjon.selvbetjening.skattetrekk.service.BehandleTrekkService
import no.nav.pensjon.selvbetjening.skattetrekk.service.HentSkattOgTrekkService
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest
class SkattetrekkControllerTest(
    @Autowired private val mockMvc: MockMvc
) {

    @MockitoBean
    lateinit var skattetrekkService: HentSkattOgTrekkService
    @MockitoBean
    lateinit var behandleTrekkService: BehandleTrekkService

    @Disabled
    @Test
    fun `happy case`() {
        every { skattetrekkService.getSkattetrekk("") } returns byggskattetrekk()

        mockMvc.perform(get("/api/bankAccount?id=1"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    private fun byggskattetrekk(): FrivilligSkattetrekkInitResponse {
        return FrivilligSkattetrekkInitResponse(
            tilleggstrekk = null,
            framtidigTilleggstrekk = null,
            skattetrekk = ForenkletSkattetrekk(
                trekkvedtakId = null,
                tabellNr = null,
                prosentsats = null
            )
        )
    }
}