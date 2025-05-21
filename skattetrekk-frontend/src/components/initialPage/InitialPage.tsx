import {
    Accordion,
    Alert,
    BodyLong,
    BodyShort,
    Box,
    Button,
    GuidePanel,
    Heading,
    HStack,
    Link,
    List,
    Loader,
    VStack
} from "@navikt/ds-react";
import React, {useContext, useEffect} from "react";
import {RegistrerteSkattetrekk} from "@/components/initialPage/RegistrerteSkattetrekk";
import {useNavigate} from "react-router-dom";
import {MessageCode, SatsType, saveSkattetrekk} from "@/api/skattetrekkBackendClient";
import {DataContext} from "@/state/DataContextProvider";
import {PageLinks} from "@/routes";
import {StopTilleggstrekkConfirmationModal} from "@/components/initialPage/StopTilleggstrekkConfirmationModal";
import {resetFormState} from "@/state/FormState";

export function InitialPage() {
    const {initiateResponse, setInitiateResponse} = useContext(DataContext)

    const navigate = useNavigate()
    const pid = new URLSearchParams(document.location.search).get("pid")

    useEffect(() => {
        resetFormState()
    })

    function onClickContinue() {
        navigate(import.meta.env.BASE_URL + PageLinks.ENDRING, {
            state: {
                pid: pid
            }
        })
    }

    async function stopTilleggstrekk() {
        const response = await saveSkattetrekk(
            {
                value: 0,
                satsType: SatsType.KRONER,
            })
        setInitiateResponse(response)
    }

    function isDecember() {
        const currentDate = new Date();
        return currentDate.getMonth() === 11;
    }

    function getYear() {
        const currentDate = new Date();
        return currentDate.getFullYear();
    }


    if(initiateResponse === null) {
        return (
            <Box background="bg-subtle" padding="16" borderRadius="large">
                <VStack align="center" gap="20">
                    <Loader size="3xlarge" />
                    <BodyShort align="center">{"Vent mens vi laster inn siden."}</BodyShort>
                </VStack>
            </Box>
        )
    }

    if (initiateResponse?.messages?.find(message => message.code === MessageCode.SERVICE_UNAVAILABLE)) { //TODO code?
        return (
            <VStack gap="6">
                {guidePanel()}
                <Alert variant="warning">
                    <Heading spacing size="small" level="3">
                        Tjenesten er ikke åpen nå
                    </Heading>
                    <BodyLong spacing>Av tekniske årsaker er registrering av frivillig skattetrekk i denne tjenesten kun
                        åpent mandag-fredag 06:00 til 21:30. Offentlige fridager og helger er denne tjenesten ofte
                        stengt.</BodyLong>
                    <BodyLong><strong>I åpningstidene kan du gjøre dette i denne tjenesten</strong></BodyLong>
                    <li>se registrert frivillig skattetrekk </li>
                    <li>registrere frivillig skattetrekk </li>
                    <li>stoppe frivillig skattetrekk </li>
                </Alert>
            </VStack>
        )
    }

    return (
        <VStack gap="8">
            <VStack gap="6">

                { guidePanel()}

                {isDecember() &&
                    <Alert variant={"info"}>
                        <VStack gap="5">
                            <BodyLong> Frivillig skattetrekk du legger inn nå, vil gjelde for {getYear() + 1}. </BodyLong>
                            <BodyLong> Når skattekortet for {getYear() + 1} kommer i slutten av desember, blir det oppdatert
                                her. Frem til da vises årets skattekort.</BodyLong>
                        </VStack>
                    </Alert>
                }
            </VStack>

            <VStack gap="16">
                {initiateResponse?.data &&
                    <VStack gap={{xs: "2", md: "6"}}>
                        <Heading size={"medium"} level="2">Dine registrerte skattetrekk</Heading>

                        <RegistrerteSkattetrekk skatteTrekk={initiateResponse.data.skattetrekk!} tilleggstrekk={initiateResponse.data.tilleggstrekk} framtidigTilleggstrekk={initiateResponse.data.framtidigTilleggstrekk} isDecember={isDecember()} />
                        {/*TODO clarify logic below with gard*/}
                        {initiateResponse.data.tilleggstrekk !== null || initiateResponse.data.framtidigTilleggstrekk?.sats !== 0 &&
                            <StopTilleggstrekkConfirmationModal onConfirm={stopTilleggstrekk}/>}
                    </VStack> }

                <Accordion>
                    <Accordion.Item>
                        <Accordion.Header>Slik trekker Nav frivillig skattetrekk</Accordion.Header>
                        <Accordion.Content>
                            <BodyLong spacing>Trekket du registrerer kommer i tillegg til det ordinære skattetrekket. Frivillig skattetrekk gjelder også ved utbetaling av feriepenger og
                                for perioder hvor det ellers ikke blir trukket skatt. Det kan ikke trekkes frivillig skatt på skattefrie pengestøtter.
                                Tilleggstrekket legges inn som et fast kronebeløp eller som et fast prosenttrekk per måned. </BodyLong>
                            <Link href={"https://www.nav.no/frivillig-skattetrekk"}>Les om frivillig skattetrekk</Link>

                        </Accordion.Content>
                    </Accordion.Item>
                    <Accordion.Item>
                        <Accordion.Header>Så lenge varer frivillig skattetrekk</Accordion.Header>
                        <Accordion.Content>
                            <BodyLong>Frivillig skattetrekk vil gjelde fra og med måneden etter at du har lagt det inn.
                                Det stoppes automatisk ved årsskiftet. Du må legge inn nytt trekk for hvert nytt år.
                                Tilleggstrekk lagt til i desember vil gjelde fra januar og ut neste år.</BodyLong>
                        </Accordion.Content>
                    </Accordion.Item>
                    <Accordion.Item>
                        <Accordion.Header>Utbetalinger som kan ha frivillig skattetrekk</Accordion.Header>
                        <Accordion.Content>
                            <VStack gap="4">
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
                                <BodyLong>Frivillig skattetrekk registrert i denne tjenesten vil kun føre til trekk hvis du har utbetaling av pengestøttene i kulepunktlisten over.</BodyLong>
                                <BodyLong> Noen pengestøtter kan ikke gis frivillig skattetrekk fordi de er
                                    skattefrie.</BodyLong>
                                <BodyLong>Barnepensjon kan få frivillig skattetrekk, men det kan desverre ikke registreres i denne tjenesten. <Link target="_blank" href="https://www.nav.no/frivillig-skattetrekk">Les om hvordan registrere frivillig skattetrekk på barnepensjon. </Link>
                                </BodyLong>

                            </VStack>
                        </Accordion.Content>
                    </Accordion.Item>
                </Accordion>

            </VStack>

            <HStack>
                <Button variant="primary" onClick={onClickContinue}>{initiateResponse?.data?.tilleggstrekk ?
                    "Endre frivillig skattetrekk" : "Start registrering"}</Button>
            </HStack>
        </VStack>
    )

    function guidePanel() {
        return (
            <GuidePanel poster>
                <BodyLong>
                    Nav trekker skatt på bakgrunn av ditt skattekort som Nav har mottatt fra Skatteetaten. Hvis du ønsker å trekke mer skatt av pengestøtten din fra Nav,
                    kan du registrere et frivillig skattetrekk.
                </BodyLong>
            </GuidePanel>
        )
    }
}