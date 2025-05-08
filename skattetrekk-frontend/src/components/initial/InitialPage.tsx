import {
    Alert,
    BodyLong,
    Heading,
    Link,
    ReadMore,
    VStack
} from "@navikt/ds-react";
import React, {useContext, useState} from "react";
import {RegistrerteSkattetrekk} from "@/components/initial/RegistrerteSkattetrekk";
import {useLoaderData, useNavigate} from "react-router-dom";
import {FrivilligSkattetrekkInitResponse, SatsType, saveSkattetrekk} from "@/api/skattetrekkBackendClient";
import {FormStateContext} from "@/state/FormState";
import {Selector} from "@/components/initial/Selector";
import {DataContext} from "@/state/DataContextProvider";

export function InitialPage() {
    const {setTilleggstrekkType, setTilleggstrekkValue} = useContext(FormStateContext)
    const {initiateResponse, setSendResponse} = useContext(DataContext)
    //
    const [buttonIsLoading, setButtonIsLoading] = useState(false)

    const skattetrekkLoader = useLoaderData() as FrivilligSkattetrekkInitResponse
    const pid = new URLSearchParams(document.location.search).get("pid")

    const navigate = useNavigate()


    async function submitTilleggstrekk(type: SatsType, value: number | null) {
        try {
            if (type !== null && value !== null) {
                setButtonIsLoading(true)
                setTilleggstrekkType(type)
                setTilleggstrekkValue(value)

                console.log(type, value)

                const isSuccess = await saveSkattetrekk(
                    {
                        trekkVedtakId: initiateResponse?.tilleggstrekk?.trekkvedtakId || "",
                        value: value,
                        satsType: type
                    });

                if (isSuccess) {
                    setSendResponse(true)
                    navigate(import.meta.env.BASE_URL + "/kvittering", {
                        state: {
                            pid: pid
                        }
                    })
                }
            }
            setButtonIsLoading(false)
        } catch (e) {
            setButtonIsLoading(false)
        }
    }

    function showDecemberMessage() {
        const currentDate = new Date();
        return currentDate.getMonth() === 11;
    }

    function getYear() {
        const currentDate = new Date();
        return currentDate.getFullYear();
    }

    return (
        <VStack gap="16">
            <VStack gap="6" id="samboer-historikk-tittel">
                {initiateResponse?.skattetrekk?.trekkvedtakId != null &&
                    <Alert variant={"warning"}>
                        Du har ikke en skattepliktig ytelse fra Nav. Du kan derfor ikke legge inn et frivillig skattetrekk.
                    </Alert>}

                {showDecemberMessage() &&
                    <Alert variant={"info"}>
                        <VStack gap="5">
                            <BodyLong> Frivillig skattetrekk du legger inn nå, vil gjelde for {getYear()}. </BodyLong>
                            <BodyLong> Når skattekortet for {getYear()} kommer i slutten av desember, blir det oppdatert her. Frem til da vises årets skattekort.</BodyLong>
                        </VStack>
                    </Alert>
                }


                <BodyLong>
                    Nav trekker skatt på bakgrunn av ditt skattekort som Nav har mottatt fra Skatteetaten.
                    Hvis du ønsker å trekke mer skatt av pengestøtten din fra Nav, kan du registrere et frivillig skattetrekk her.
                    Trekket kommer i tillegg til det ordinære skattetrekket. Frivillig skattetrekk gjelder også ved utbetaling av feriepenger og for
                    perioder hvor det ellers ikke blir trukket skatt. Det kan ikke registreres frivillig skattetrekk i skattefrie pengestøtter.
                </BodyLong>

                <ReadMore header="Disse pengestøttene kan du regstrere frivillig skattetrekk i">
                    <BodyLong spacing>
                        tekst
                    </BodyLong>
                </ReadMore>

                <BodyLong>
                    Tilleggstrekket legges inn som et fast kronebeløp eller som et fast prosenttrekk per måned
                    og vil gjelde fra og med måneden etter at du har lagt det inn. Det stoppes automatisk ved årsskiftet.
                    Du må legge inn nytt trekk for hvert nytt år. Tilleggstrekk lagt til i desember vil gjelde fra januar og ut neste år.
                </BodyLong>

                <Link href="nav.no">Les om frivillig skattetrekk</Link>
            </VStack>

            <VStack gap={{xs: "2", md: "6"}}>
                <Heading size={"medium"} level="2">Dine registrerte skattetrekk</Heading>
                <RegistrerteSkattetrekk skatteTrekk={skattetrekkLoader.skattetrekk} tilleggstrekk={skattetrekkLoader.tilleggstrekk} />
            </VStack>

            <Selector submitTilleggstrekk={submitTilleggstrekk} maxKroner={10000} buttonIsLoading={buttonIsLoading}/>
        </VStack>


    )
}