import {Accordion, Alert, BodyLong, GuidePanel, Heading, Link, List, VStack} from "@navikt/ds-react";
import React, {useContext} from "react";
import {RegistrerteSkattetrekk} from "@/components/initialPage/RegistrerteSkattetrekk";
import {useNavigate} from "react-router-dom";
import {MessageType} from "@/api/skattetrekkBackendClient";
import {FormStateContext} from "@/state/FormState";
import {DataContext} from "@/state/DataContextProvider";
import {PageLinks} from "@/routes";
import {StopTilleggstrekkConfirmationModal} from "@/components/initialPage/StopTilleggstrekkConfirmationModal";

export function InitialPage() {
    const {setTilleggstrekkType, setTilleggstrekkValue} = useContext(FormStateContext)
    const {initiateResponse} = useContext(DataContext)

    const navigate = useNavigate()
    const pid = new URLSearchParams(document.location.search).get("pid")


    function onClickContinue() {
        navigate(import.meta.env.BASE_URL + PageLinks.ENDRING, {
            state: {
                pid: pid
            }
        })
    }


    async function stopTilleggstrekk() {
        //todo
    }

    function showDecemberMessage() {
        const currentDate = new Date();
        return currentDate.getMonth() === 11;
    }

    function getYear() {
        const currentDate = new Date();
        return currentDate.getFullYear();
    }

    console.log("initiateResponse", initiateResponse)

    return (
        <VStack gap="8">
            <VStack gap="6" id="samboer-historikk-tittel">

                <GuidePanel poster>
                    <BodyLong>
                        Nav trekker skatt på bakgrunn av ditt skattekort som Nav har mottatt fra Skatteetaten. Hvis du
                        ønsker å trekke mer skatt av pengestøtten din fra Nav, kan du registrere et frivillig
                        skattetrekk her.
                    </BodyLong>
                </GuidePanel>


                {showDecemberMessage() &&
                    <Alert variant={"info"}>
                        <VStack gap="5">
                            <BodyLong> Frivillig skattetrekk du legger inn nå, vil gjelde for {getYear()}. </BodyLong>
                            <BodyLong> Når skattekortet for {getYear()} kommer i slutten av desember, blir det oppdatert
                                her. Frem til da vises årets skattekort.</BodyLong>
                        </VStack>
                    </Alert>
                }
            </VStack>



            <VStack gap="16">
                {initiateResponse?.data &&
                    <VStack gap={{xs: "2", md: "6"}}>
                        <Heading size={"medium"} level="2">Dine registrerte skattetrekk</Heading>
                        {
                            initiateResponse?.messages?.map((message) => {
                                if (message.type === MessageType.INFO) {
                                    return (
                                        <Alert variant="info">
                                            Det frivillige skattetrekket er stoppet fra og med neste måned.
                                        </Alert>
                                    )
                                }
                            })
                        }

                        <RegistrerteSkattetrekk skatteTrekk={initiateResponse.data.skattetrekk!} tilleggstrekk={initiateResponse.data.tilleggstrekk} framtidigTilleggstrekk={initiateResponse.data.framtidigTilleggstrekk} />
                        {initiateResponse.data.tilleggstrekk === null &&
                            <StopTilleggstrekkConfirmationModal onConfirm={stopTilleggstrekk}/>}
                    </VStack> }

                <Accordion>
                    <Accordion.Item>
                        <Accordion.Header>Slik trekker Nav frivillig skattetrekk</Accordion.Header>
                        <Accordion.Content>
                            <BodyLong spacing>Trekket du registrerer kommer i tillegg til det ordinære skattetrekket.
                                Frivillig skattetrekk gjelder også ved utbetaling av feriepenger og
                                for perioder hvor det ellers ikke blir trukket skatt. Det kan ikke registreres frivillig
                                skattetrekk i skattefrie pengestøtter.</BodyLong>
                            <Link href={"#"}>Les om frivillig skattetrekk</Link>
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
                                <BodyLong>Frivillig skattetrekk registrert i denne tjenesten vil kun føre til trekk hvis
                                    du har utbetaling av pengestøttene i kulepunktlisten.</BodyLong>
                                <BodyLong> Barnepensjon kan også ha frivillig skattetrekk, men det kan desverre ikke
                                    registreres i denne tjenesten.</BodyLong>
                                <BodyLong> Noen pengestøtter kan ikke gis frivillig skattetrekk fordi de er
                                    skattefrie.</BodyLong>
                            </VStack>
                        </Accordion.Content>
                    </Accordion.Item>
                </Accordion>

                {/*<Selector submitTilleggstrekk={submitTilleggstrekk} maxKroner={10000}*/}
                {/*          buttonIsLoading={buttonIsLoading}/>*/}
            </VStack>

            {/*show a horizontal black line, to separate the sections in the page*/}
            {/*<hr style={{ border: "1px solid black", width: "100%" }} />*/}

            <HStack>
                <Button variant="primary" onClick={onClickContinue}>Start registrering</Button>
            </HStack>



        </VStack>


    )
}