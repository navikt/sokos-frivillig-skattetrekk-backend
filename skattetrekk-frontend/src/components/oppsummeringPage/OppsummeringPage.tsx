import {BodyLong, BodyShort, Box, Button, FormSummary, Heading, HStack, Loader, VStack} from '@navikt/ds-react'
import React, {useContext, useState} from 'react'
import {ForenkletSkattetrekk, SatsType, saveSkattetrekk} from "@/api/skattetrekkBackendClient";
import {FormStateContext} from "@/state/FormState";
import {DataContext} from "@/state/DataContextProvider";
import {useLocation, useNavigate} from "react-router-dom";
import {numberFormatWithKr, showPercentageOrTable, visProsentEllerBelop} from "@/common/Utils";
import {PageLinks} from "@/routes";

export const OppsummeringPage = () => {
    const {tilleggstrekkType} = useContext(FormStateContext)
    const {setSendResponse} = useContext(DataContext)
    const [isSending, setIsSending] = useState(false)
    const navigate = useNavigate()
    const location = useLocation()
    window.history.state.usr = location

    const pid = new URLSearchParams(document.location.search).get("pid")

    async function submitTilleggstrekk() {
        if (tilleggstrekkType !== null && location.state.tilleggstrekkValue !== null) {
            setIsSending(true)
            const response = await saveSkattetrekk(
                {
                    value: location.state.tilleggstrekkValue,
                    satsType: tilleggstrekkType,
                })
            setSendResponse(response)
            setIsSending(false)
            navigate(import.meta.env.BASE_URL + PageLinks.KVITTERING, {
                state: {
                    pid: pid
                }
            })
        }
    }

    function sumStrekkString(
            skattetrekk: ForenkletSkattetrekk | null,
            tilleggstrekkType: SatsType | null,
            tilleggstrekkValue: number | null) {

        let result: string
        if (tilleggstrekkType === SatsType.PROSENT && tilleggstrekkValue != null) {
            return ((skattetrekk?.prosentsats) ? skattetrekk.prosentsats : + tilleggstrekkValue!)  + " %"
        }
        if (tilleggstrekkType === SatsType.PROSENT) {
            result = tilleggstrekkValue + " %"
        } else {
            result = numberFormatWithKr(tilleggstrekkValue!) + " kr per måned"
        }

        result += " i tillegg til"

        if (skattetrekk?.prosentsats != null) {
            result += ` ${skattetrekk} % fra skattekortet`
        } else {
            result += " tabelltrekket"
        }

        return result
    }

    if(isSending) {
        return (
            <Box background="bg-subtle" padding="16" borderRadius="large">
                <VStack align="center" gap="8">
                    <Heading align="center" size={"large"} level="2">
                        Vent mens vi sender inn
                    </Heading>
                    <Loader size="3xlarge" />
                    <BodyShort align="center">Dette kan ta opptil ett minutt.</BodyShort>
                </VStack>
            </Box>
        )
    }

  return (
      <VStack gap="12">
          <Heading level="2" size="large">Oppsummering</Heading>
          <BodyLong>Nå kan du se over at alt er riktig før du registrerer det frivillige skattetrekket.</BodyLong>

      <FormSummary>
          <FormSummary.Header>
              <FormSummary.Heading level="2">Skattetrekk</FormSummary.Heading>
              <FormSummary.EditLink onClick={() => navigate(import.meta.env.BASE_URL + PageLinks.ENDRING, {state: {pid: pid}})} />
          </FormSummary.Header>


          <FormSummary.Answers>
              <FormSummary.Answer>
                  <FormSummary.Label>Frivillig skattetrekk</FormSummary.Label>
                  <FormSummary.Value>{visProsentEllerBelop({sats:location.state.tilleggstrekkValue, satsType:location.state.tilleggstrekkType})}</FormSummary.Value>
              </FormSummary.Answer>
              <FormSummary.Answer>
                    <FormSummary.Label>Skattekort</FormSummary.Label>
                    <FormSummary.Value>{showPercentageOrTable(location.state.skattetrekk!)}</FormSummary.Value>
              </FormSummary.Answer>
              <FormSummary.Answer>
                  <Box padding="4" background="surface-subtle" borderRadius="large">
                      <VStack gap={{ xs: '2', sm: '1' }}>
                          <BodyLong size="medium" style={{ fontSize: "1.1rem" }}>
                              <strong>Skattetrekk til sammen med din endring</strong>
                          </BodyLong>
                          <BodyLong className="sum" size={"large"} style={{ fontSize: "1.5rem" }}>
                              <strong>{sumStrekkString(
                                  location.state.skattetrekk,
                                  location.state.tilleggstrekkType,
                                  location.state.tilleggstrekkValue
                              )}</strong>
                          </BodyLong>
                      </VStack>
                  </Box>
                </FormSummary.Answer>
          </FormSummary.Answers>
      </FormSummary>

          <VStack gap="6">
              <HStack gap="2">
                  <Button variant="secondary" size={"medium"} onClick={() => navigate(import.meta.env.BASE_URL + PageLinks.ENDRING, {state: {pid: pid}})}>
                        Tilbake
                  </Button>
                  <Button variant="primary" size={"medium"} type={"submit"}
                        onClick={submitTilleggstrekk}> Registrer </Button>
              </HStack>
              <HStack gap="2">
                  <Button variant="tertiary" size={"medium"} onClick={() => navigate(import.meta.env.BASE_URL + PageLinks.INDEX, {state: {pid: pid}})}> Avbryt </Button>
              </HStack>
          </VStack>
      </VStack>
  )
}
