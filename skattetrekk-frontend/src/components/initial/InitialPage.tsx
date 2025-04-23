import {BodyLong, Button, Heading, HStack, Radio, RadioGroup, ReadMore, TextField, VStack} from "@navikt/ds-react";
import {useCallback, useContext, useState} from "react";
import {RegistrerteSkattetrekk} from "@/components/initial/RegistrerteSkattetrekk";
import {useLoaderData, useNavigate} from "react-router-dom";
import {FrivilligSkattetrekkInitResponse} from "@/api/skattetrekkBackendClient";
import {FormStateContext} from "@/state/FormState";
import {Selector} from "@/components/initial/Selector";

export function InitialPage() {
    const {setTilleggstrekkType, setTilleggstrekkValue} = useContext(FormStateContext)
    //
    // const [buttonIsLoading, setButtonIsLoading] = useState(false)

    const skattetrekkLoader = useLoaderData() as FrivilligSkattetrekkInitResponse
    const pid = new URLSearchParams(document.location.search).get("pid")

    const navigate = useNavigate()


    // async function submitTilleggstrekk() {
    //     try {
    //         setButtonIsLoading(true)
    //         //TODO: send til backend
    //         setButtonIsLoading(false)
    //         navigate(import.meta.env.BASE_URL + "/kvittering", {
    //             state: {
    //                 pid: pid,
    //                 //response: response
    //             }
    //         })
    //     } catch (e) {
    //         setButtonIsLoading(false)
    //     }
    // }

    return (
        <VStack gap="8">
            <VStack gap="6" spacing="4" id="samboer-historikk-tittel">
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

                <BodyLong spacing>
                    Tilleggstrekket legges inn som et fast kronebeløp eller som et fast prosenttrekk pr.
                    måned og vil gjelde fra og med måneden etter at du har lagt det inn. Det stoppes automatisk ved årsskiftet.
                    Du må legge inn nytt trekk for hvert nytt år. Tilleggstrekk lagt til i desember vil gjelde fra januar og ut neste år.
                </BodyLong>
            </VStack>

            <VStack gap="4" spacing="4">
                <Heading size={"medium"} level="2">Dine registrerte skattetrekk</Heading>
                <RegistrerteSkattetrekk skatteTrekk={skattetrekkLoader.skattetrekk} tilleggstrekk={skattetrekkLoader.tilleggstrekk} />
            </VStack>

            <Selector setType={setTilleggstrekkType} setValue={setTilleggstrekkValue}/>

            {/*<HStack gap="2">*/}
            {/*    <Button variant="primary" size={"medium"} loading={buttonIsLoading}*/}
            {/*            onClick={submitTilleggstrekk}> Registrer </Button>*/}
            {/*    <Button variant="tertiary" size={"medium"}> Avbryt </Button>*/}
            {/*</HStack>*/}
        </VStack>


    )
}