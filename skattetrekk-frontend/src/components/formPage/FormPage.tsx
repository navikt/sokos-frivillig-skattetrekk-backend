import {BodyLong, Button, Heading, HStack, Link, List, Radio, RadioGroup, TextField, VStack} from '@navikt/ds-react'
import React, {useContext, useEffect, useState} from 'react'
import {SatsType, saveSkattetrekk} from "@/api/skattetrekkBackendClient";
import {DataContext} from "@/state/DataContextProvider";
import {numberFormatWithKr, parseInntekt} from "@/common/Utils";
import {PageLinks} from "@/routes";
import {FormStateContext} from "@/state/FormState";
import {useNavigate} from "react-router-dom";

export const FormPage = () => {
    const {initiateResponse, setSendResponse} = useContext(DataContext)
    const {isEligible, tilleggstrekkType, tilleggstrekkValue, setTilleggstrekkType, setTilleggstrekkValue, setIsEligible} = useContext(FormStateContext)

    const [type, setType] = useState<SatsType | null>(tilleggstrekkType)
    const [value, setValue] = useState<string | null>(tilleggstrekkValue?.toString() ?? null)

    const [buttonIsLoading, setButtonIsLoading] = useState(false)
    const [pageState, setPageState] = useState<"initial" | "cannotProceed">("initial")
    const [shouldValidateForm, setShouldValidateForm] = useState(false)

    const [canContinue, setCanContinue] = useState<boolean | null>(isEligible)
    const [canContinueError, setCanContinueError] = useState<string | null>(null)
    const [selectorError, setSelectorError] = useState<string | null>(null)
    const [valueError, setValueError] = useState<string | null>(null)
    const navigate = useNavigate()
    const pid = new URLSearchParams(document.location.search).get("pid")

    useEffect(() => {
        window.scrollTo(0, 0);
    }, [pageState]);

    async function onClickNext() {
        setShouldValidateForm(true)
        if(validateInput()) {
            if (type !== null && value !== null) {
                setButtonIsLoading(true)
                setIsEligible(canContinue)
                setTilleggstrekkType(type)
                var tillleggstrekkValue = parseInntekt(value)
                setTilleggstrekkValue(tillleggstrekkValue)
                var response = await saveSkattetrekk({satsType: type, value: tillleggstrekkValue })
                    setSendResponse(response)
                    navigate(import.meta.env.BASE_URL + PageLinks.OPPSUMMERING, {
                        state: {
                            pid: pid
                        }
                    })
                setButtonIsLoading(false)
            }
        }
    }

    const onChangeType = (val: SatsType) => {
        setType(val)
        setValue(null)
        setSelectorError(null)
        setValueError(null)
    }

    const validateInput = (): boolean => {
        const numericValue = parseInntekt(value)

        if(canContinue === null) {
            setCanContinueError("Du må svare på om du har en av pengestøttene på kulepunktlisten")
        } else if (canContinue === false) {
            setPageState("cannotProceed")
        } else if (type === null) {
            setSelectorError("Du må velge hvilken type frivillig skattetrekk du ønsker")
        } else if (value === '' || value === null) {
            setValueError("Du må oppgi et beløp")
        } else if (isNaN(numericValue) || numericValue < 0) {
            setValueError('Du kan ikke skrive bokstaver eller tegn')
        } else if (type === SatsType.PROSENT && numericValue > 100) {
            setValueError('Du kan maks oppgi 100 %')
        } else if (type === SatsType.KRONER && numericValue === 0) {
            setValueError(`Du må oppgi et høyere beløp enn 0 kr. Ønsker du å stoppe et frivilligskattetrekk? Gå tilbake og klikk på knappen “Stopp frivillig skattetrekk”.`)
        } else if (type === SatsType.PROSENT && numericValue === 0) {
            setValueError(`Du må oppgi mer enn 0 %. Ønsker du å stoppe et frivillig skattetrekk? Gå tilbake og klikk på knappen “Stopp frivillig skattetrekk”.`)
        } else if (type === SatsType.KRONER && numericValue > 99999) { //todo this value should come from initiateResponse.messages
            setValueError(`Du kan maks oppgi ${numberFormatWithKr(99999)}. Vil du trekke et høyere beløp, kan du legge det inn som prosent`)
        } else if (type === SatsType.KRONER && numericValue === 0) {
            setValueError('Du må oppgi et høyere beløp enn 0 kr')
        }

        else {
            setCanContinueError(null)
            setValueError(null)
            setValueError(null)
            return true
        }

        return false
    }

    if(pageState === "cannotProceed") {
        return (
            <VStack gap="8" className="form-container">
                <Heading level="2" size="medium">Du kan ikke registrere frivillig skattetrekk i denne tjenesten</Heading>
                <VStack>
                    <Heading level="3" size="small">
                        Ikke alle pengestøtter kan få frivillig skattetrekk
                    </Heading>
                    <BodyLong>
                        Noen pengestøtter kan ikke få frivillig skattetrekk fordi de er skattefrie.
                    </BodyLong>
                </VStack>
                <VStack>
                    <Heading level="3" size="small">
                        Barnepensjon
                    </Heading>
                    <BodyLong>
                        Frivillig skatterekk på barnepensjon kan desverre ikke registreres i denne tjenesten. <Link>Her finner du informasjon om frivillig skattetrekk og barnepensjon.</Link>                      {/*    TOOD link?*/}
                    </BodyLong>
                </VStack>

                <HStack gap="2">
                    <Button variant="secondary" size={"medium"} onClick={() => setPageState("initial")}>
                        Tilbake
                    </Button>
                    <Button variant="tertiary" size={"medium"} onClick={() => navigate(import.meta.env.BASE_URL + PageLinks.INDEX, {state: {pid: pid}})}>
                        Avbryt
                    </Button>

                </HStack>
            </VStack>
        )
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

          <RadioGroup
              legend="Har du en eller flere av pengestøttene i kulepunktlisten over?"
              onChange={(e) => {
                  setCanContinue(e === "true")
                  setCanContinueError(null)}}
              value={canContinue === null ? undefined : canContinue.toString()}
              error={canContinueError}>
              <Radio value="true">Ja</Radio>
              <Radio value="false">Nei</Radio>
          </RadioGroup>

          { canContinue &&
              <VStack gap="4">
                  <Heading size={"medium"} level={"2"}>Legg til frivillig tilleggstrekk</Heading>
                  <RadioGroup id="typeRadio"
                              legend="Hvordan skal skatten trekkes?"
                              size={"medium"}
                              description="Skattetrekk per måned"
                              value={type}
                              onChange={(v) => {
                                  onChangeType(v);
                              }}
                              onBlur={() => {
                                  if (shouldValidateForm) {
                                      validateInput();
                                  }
                              }}
                              error={selectorError}>
                      <Radio value={SatsType.PROSENT} description="Trekkes fra alle utbetalinger">Prosent</Radio>
                      <Radio value={SatsType.KRONER} description= "Trekkes vanligvis fra månedens første utbetaling">Kroner per måned</Radio>
                  </RadioGroup>
              </VStack> }

              {canContinue && type &&
                  <TextField id="tilleggstrekk_input"
                             label={type === SatsType.PROSENT ? "Hvor mange prosent?" : "Hvor mange kroner?"}
                             style={{width: "160px"}}
                             inputMode="numeric"
                             error={valueError}
                             pattern="[\d\s]+"
                             value={value ?? ""}
                             onChange={(v) => {
                                 setValue(v.target.value)
                             }}
                             onBlur={() => {
                                 if (shouldValidateForm) {
                                     validateInput();
                                 }
                             }}
                             htmlSize={30}
                  />
              }



          <VStack gap={"4"}>
              <HStack gap="2">
                  <Button variant="secondary" size={"medium"} onClick={() => navigate(import.meta.env.BASE_URL + PageLinks.INDEX, {state: {pid: pid}})}>
                      Tilbake
                  </Button>
                  <Button variant="primary" size={"medium"} loading={buttonIsLoading} type={"submit"}
                          onClick={onClickNext}> Neste </Button>
              </HStack>
            <HStack>
                <Button variant="tertiary" size={"medium"} onClick={() => navigate(import.meta.env.BASE_URL + PageLinks.INDEX, {state: {pid: pid}})}> Avbryt </Button>
            </HStack>
          </VStack>



      </VStack>
  )
}
