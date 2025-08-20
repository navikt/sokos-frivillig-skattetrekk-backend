package no.nav.frivillig.skattetrekk.endpoint

import io.mockk.every
import no.nav.frivillig.skattetrekk.endpoint.api.ForenkletSkattetrekkDto
import no.nav.frivillig.skattetrekk.endpoint.api.FrivilligSkattetrekkData
import no.nav.frivillig.skattetrekk.endpoint.api.FrivilligSkattetrekkInitResponse
import no.nav.frivillig.skattetrekk.service.BehandleTrekkService
import no.nav.frivillig.skattetrekk.service.HentSkattOgTrekkService
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Disabled
@WebMvcTest
class SkattetrekkControllerTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockitoBean
    lateinit var skattetrekkService: HentSkattOgTrekkService

    @MockitoBean
    lateinit var behandleTrekkService: BehandleTrekkService

    @Disabled
    @Test
    fun `happy case`() {
        every { skattetrekkService.hentSkattetrekk("") } returns byggskattetrekk()

        mockMvc
            .perform(get("/api/skattetrekk"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    private fun byggskattetrekk(): FrivilligSkattetrekkInitResponse =
        FrivilligSkattetrekkInitResponse(
            messages = emptyList(),
            data =
                FrivilligSkattetrekkData(
                    tilleggstrekk = null,
                    fremtidigTilleggstrekk = null,
                    skattetrekk =
                        ForenkletSkattetrekkDto(
                            tabellNr = null,
                            prosentsats = null,
                        ),
                    maxBelop = 10000,
                    maxProsent = 100,
                ),
        )
}
