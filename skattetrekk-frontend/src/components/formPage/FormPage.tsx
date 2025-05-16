import {
    BodyLong,
    BodyShort,
    Box,
    Button,
    FormSummary,
    Heading,
    HStack,
    List, Radio,
    RadioGroup,
    VStack
} from '@navikt/ds-react'
import React, {useContext, useState} from 'react'
import {SatsType, saveSkattetrekk} from "@/api/skattetrekkBackendClient";
import {FormStateContext} from "@/state/FormState";
import {DataContext} from "@/state/DataContextProvider";
import {useNavigate} from "react-router-dom";
import {showPercentageOrTable, visProsentEllerBelop} from "@/common/Utils";
import {PageLinks} from "@/routes";
import {ListItem} from "@navikt/ds-react/cjs/list";
import {Selector} from "@/components/formPage/Selector";

export const FormPage = () => {
    const {setTilleggstrekkType, setTilleggstrekkValue} = useContext(FormStateContext)
    const {initiateResponse, setSendResponse} = useContext(DataContext)
    const [buttonIsLoading, setButtonIsLoading] = useState(false)
    const [pageState] = useState<"initial" | string>("initial")
    const [canContinue, setCanContinue] = useState<boolean | null>(null)
    const navigate = useNavigate()
    const pid = new URLSearchParams(document.location.search).get("pid")

    async function submitTilleggstrekk(type: SatsType, value: number | null) {
        if (type !== null && value !== null) {
            setButtonIsLoading(true)
            setTilleggstrekkType(type)
            setTilleggstrekkValue(value)

            navigate(import.meta.env.BASE_URL + PageLinks.OPPSUMMERING, {
                state: {
                    pid: pid
                }
            })
        }
    }

    // async function submitTilleggstrekk() {
    //     if (tilleggstrekkType !== null && tilleggstrekkValue !== null) {
    //         setButtonLoadinhg(true)
    //         const response = await saveSkattetrekk(
    //             {
    //                 data: {
    //                     value: tilleggstrekkValue,
    //                     satsType: tilleggstrekkType,
    //                 }
    //             })
    //
    //         setSendResponse(response)
    //         setButtonLoadinhg(false)
    //         navigate(import.meta.env.BASE_URL + PageLinks.KVITTERING, {
    //             state: {
    //                 pid: pid
    //             }
    //         })
    //     }
    // }
    //
    // function sumStrekkString(){
    //     var result: string
    //     if (tilleggstrekkType === SatsType.PROSENT && initiateResponse?.data!.skattetrekk?.prosentsats != null) {
    //         return (initiateResponse?.data.skattetrekk?.prosentsats + tilleggstrekkValue!) + " %"
    //     }
    //     if (tilleggstrekkType === SatsType.PROSENT) {
    //         result = tilleggstrekkValue + " %"
    //     } else {
    //         result = tilleggstrekkValue + " kr per måned"
    //     }
    //
    //     result += " i tillegg til"
    //     if (initiateResponse?.data!.skattetrekk?.prosentsats != null) {
    //         result += ` ${initiateResponse?.data.skattetrekk?.prosentsats} % fra skattekortet`
    //     } else {
    //         result += " tabelltrekket"
    //     }
    //
    //     return result
    // }
    const handleChange = (val: string) => {


    }

  return (
      <VStack gap="12">
          <VStack gap={"6"}>
          <Heading level="2" size="medium">Dette kan du registrere frivillig skattetrekk på her:</Heading>
              <BodyLong>
                  Bare skattepliktige pengestøtter kan få frivillig skattetrekk.
                  <List>
                        <List.Item>Arbeidsavklaringspenger (AAP)</List.Item>
                        <List.Item>Dagpenger</List.Item>
                        <List.Item>Foreldre- og svangerskapspenger</List.Item>
                        <List.Item>Omstillingsstønad</List.Item>
                        <List.Item>Overgangsstønad til enslig mor eller far</List.Item>
                        <List.Item>Pensjon fra Nav</List.Item>
                        <List.Item>Pensjon fra Statens pensjonskasse (SPK)</List.Item>
                        <List.Item>Pleie-, omsorg- og opplæringspenger</List.Item>
                        <List.Item>Sykepenger</List.Item>
                        <List.Item>Supplerende stønad alder</List.Item>
                        <List.Item>Supplerende stønad uføre</List.Item>
                        <List.Item>Uførepensjon fra Statens pensjonskasse (SPK)</List.Item>
                        <List.Item>Uføretrygd</List.Item>
                  </List>
              </BodyLong>
          </VStack>

          <RadioGroup legend="Har du en eller flere av pengestøttene i kulepunktlisten over?" onChange={setCanContinue}>
              <Radio value="true">Ja</Radio>
              <Radio value="false">Nei</Radio>
          </RadioGroup>

          { canContinue &&
              <Selector submitTilleggstrekk={submitTilleggstrekk} maxKroner={10000} buttonIsLoading={buttonIsLoading}/>}


          <VStack gap={"4"}>
              <HStack gap="2">
                  <Button variant="secondary" size={"medium"} loading={buttonIsLoading} type={"submit"}>Tilbake</Button>
                  {/*<Button variant="primary" size={"medium"}  ctype={"submit"}*/}
                  {/*        onClick={()=>{}}> Neste </Button>*/}
              </HStack>
              <HStack>
                  <Button variant="tertiary" size={"medium"}> Avbryt </Button>
              </HStack>
          </VStack>



      </VStack>
  )
}
