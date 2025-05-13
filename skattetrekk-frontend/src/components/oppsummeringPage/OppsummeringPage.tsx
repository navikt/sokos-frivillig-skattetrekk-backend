import {BodyShort, Box, Button, FormSummary, Heading, HStack, VStack} from '@navikt/ds-react'
import React, {useContext, useState} from 'react'
import {SatsType, saveSkattetrekk} from "@/api/skattetrekkBackendClient";
import {FormStateContext} from "@/state/FormState";
import {DataContext} from "@/state/DataContextProvider";
import {useNavigate} from "react-router-dom";
import {FormatKroner} from "@/common/FormatKroner";
import {showPercentageOrTable, visProsentEllerBelop} from "@/common/Utils";
import {ArrowLeftIcon} from "@navikt/aksel-icons";

export const OppsummeringPage = () => {
    const {tilleggstrekkType, tilleggstrekkValue} = useContext(FormStateContext)
    const {initiateResponse, setSendResponse} = useContext(DataContext)
    const [buttonLoading, setButtonLoadinhg] = useState(false)

    const navigate = useNavigate()
    const pid = new URLSearchParams(document.location.search).get("pid")

    async function submitTilleggstrekk() {
        if (tilleggstrekkType !== null && tilleggstrekkValue !== null) {
            setButtonLoadinhg(true)
                const response = await saveSkattetrekk(
                    {
                        value: tilleggstrekkValue,
                        satsType: tilleggstrekkType,
                    });

                setSendResponse(response)
            setButtonLoadinhg(false)
                navigate(import.meta.env.BASE_URL + "/kvittering", {
                    state: {
                        pid: pid
                    }
                })
            }
    }

    function sumStrekkString(){
        var result: string
        if (tilleggstrekkType === SatsType.PROSENT && initiateResponse?.skattetrekk.prosentsats != null) {
            return (initiateResponse?.skattetrekk.prosentsats + tilleggstrekkValue!) + " %"
        }
        if (tilleggstrekkType === SatsType.PROSENT) {
            result = tilleggstrekkValue + " %"
        } else {
            result = tilleggstrekkValue + " kr per måned"
        }

        result += " i tillegg til"
        if (initiateResponse?.skattetrekk.prosentsats != null) {
            result += ` ${initiateResponse?.skattetrekk.prosentsats} % fra skattekortet`
        } else {
            result += " tabelltrekket"
        }

        return result
    }

  return (
      <VStack gap="12">
      <FormSummary>
          <FormSummary.Header>
              <FormSummary.Heading level="2">Skattetrekk</FormSummary.Heading>
              <FormSummary.EditLink href="#" />
          </FormSummary.Header>


          <FormSummary.Answers>
              <FormSummary.Answer>
                  <FormSummary.Label>Frivillig skattetrekk</FormSummary.Label>
                  <FormSummary.Value>{visProsentEllerBelop({sats:tilleggstrekkValue, satsType:tilleggstrekkType})}</FormSummary.Value>
              </FormSummary.Answer>
              <FormSummary.Answer>
                    <FormSummary.Label>Skattekort</FormSummary.Label>
                    <FormSummary.Value>{showPercentageOrTable(initiateResponse?.skattetrekk!)}</FormSummary.Value>
              </FormSummary.Answer>
              <FormSummary.Answer>
                  <Box padding="4" background="surface-subtle" borderRadius="large">
                      <VStack gap={{ xs: '2', sm: '1' }}>
                          <Heading level="4" size="small">
                              Skattetrekk til sammen med din endring
                          </Heading>
                          <BodyShort className="sum">
                              {sumStrekkString()}
                          </BodyShort>
                      </VStack>
                  </Box>
                </FormSummary.Answer>
          </FormSummary.Answers>
      </FormSummary>

    <HStack gap="2">
        <Button variant="primary" size={"medium"} loading={buttonLoading} type={"submit"}
                onClick={submitTilleggstrekk}> Registrer </Button>
        <Button variant="tertiary" size={"medium"}> Avbryt </Button>
    </HStack>
      </VStack>
  )
}
