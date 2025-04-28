import {
    Alert,
    BodyLong,
    Button,
    Heading,
    HStack, Link,
    Radio,
    RadioGroup,
    ReadMore,
    TextField,
    VStack
} from "@navikt/ds-react";
import {useCallback, useContext, useState} from "react";
import {RegistrerteSkattetrekk} from "@/components/initial/RegistrerteSkattetrekk";
import {useLoaderData, useNavigate} from "react-router-dom";
import {FrivilligSkattetrekkInitResponse, SatsType, saveSkattetrekk} from "@/api/skattetrekkBackendClient";
import {FormStateContext} from "@/state/FormState";
import {Selector} from "@/components/initial/Selector";
import DataContextProvider, {DataContext} from "@/state/DataContextProvider";

export function InitialPage() {
    const {tilleggstrekkType, setTilleggstrekkType, tilleggstrekkValue, setTilleggstrekkValue} = useContext(FormStateContext)
    const {initiateResponse} = useContext(DataContext)
    //
    const [buttonIsLoading, setButtonIsLoading] = useState(false)
    const [selectorError, setSelectorError] = useState(false)

    const skattetrekkLoader = useLoaderData() as FrivilligSkattetrekkInitResponse
    const pid = new URLSearchParams(document.location.search).get("pid")

    const navigate = useNavigate()


    async function submitTilleggstrekk() {
        try {
            if(setTilleggstrekkType == null) {
                setSelectorError(true)
            console.log(selectorError);
            }

            setButtonIsLoading(true)
            if (tilleggstrekkType != null && tilleggstrekkValue != null) {
                saveSkattetrekk(
                    {
                        trekkVedtakId: initiateResponse?.tilleggstrekk?.trekkvedtakId || "",
                        value: tilleggstrekkValue,
                        satsType: tilleggstrekkType
                    })
            }
            setButtonIsLoading(false)
            navigate(import.meta.env.BASE_URL + "/kvittering", {
                state: {
                    pid: pid,
                    //response: response
                }
            })
        } catch (e) {
            setButtonIsLoading(false)
        }
    }

    return (
        <VStack gap="16">
            <VStack gap="6" id="samboer-historikk-tittel">
                {initiateResponse?.skattetrekk?.trekkvedtakId != null && <Alert variant={"warning"}>
                    Du har ikke en skattepliktig ytelse fra Nav. Du kan derfor ikke legge inn et frivillig skattetrekk.
                </Alert>}

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

            <VStack gap="4" spacing="4">
                <Heading size={"medium"} level="2">Dine registrerte skattetrekk</Heading>
                <RegistrerteSkattetrekk skatteTrekk={skattetrekkLoader.skattetrekk} tilleggstrekk={skattetrekkLoader.tilleggstrekk} />
            </VStack>

            <Selector setType={setTilleggstrekkType} setValue={setTilleggstrekkValue} submitTilleggstrekk={submitTilleggstrekk}/>


        </VStack>


    )
}