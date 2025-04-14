import {BodyLong, Button, Heading, HStack, Radio, RadioGroup, ReadMore, TextField} from "@navikt/ds-react";
import {useCallback, useState} from "react";
import {RegistrerteSkattetrekk} from "@/components/RegistrerteSkattetrekk";
import {useLoaderData, useNavigate} from "react-router-dom";
import {FrivilligSkattetrekkInitResponse} from "@/api/skattetrekkBackendClient";

export function FrivilligSkattetrekkView() {

    const [tilleggstrekkValue, settilleggstrekkValue] = useState<number | undefined>(undefined)
    const [tilleggstrekkType, settilleggstrekkType] = useState<string | null>(null)
    const [buttonIsLoading, setButtonIsLoading] = useState(false)
    const skattetrekkLoader = useLoaderData() as FrivilligSkattetrekkInitResponse
    const pid = new URLSearchParams(document.location.search).get("pid")

    const navigate = useNavigate()


    const cancel = () => {
        settilleggstrekkType(null)
        settilleggstrekkValue(undefined)
    }

    const onChangeValue = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
        const value = Number.parseInt(e.target.value)
        settilleggstrekkValue(Number.isNaN(value) ? undefined : value);
    },[]);


    async function submitTilleggstrekk() {
        try {
            setButtonIsLoading(true)
            //TODO: send til backend
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
        <>
            <div id="samboer-historikk-tittel">
                <br/>
                <BodyLong spacing >
                    NAV trekker skatt på bakgrunn av skattekortet ditt. Hvis du ønsker å trekke mer skatt av
                        pensjonen eller uføretrygden din, kan du registrere et tilleggstrekk her. Det gjelder fra og med
                    måneden etter at du har lagt det inn, og stoppes automatisk ved årsskiftet.
                </BodyLong>
                <BodyLong spacing>
                    Hvis du er bosatt i et annet land eller er registrert med D-nummer i Norge, kan du ikke
                    registrere et tilleggstrekk her. Du må ta kontakt med Skatteetaten.
                </BodyLong>
                <ReadMore header="Om frivillig skattetrekk">
                    <BodyLong spacing>
                        Det ordinære skattetrekket trekkes på bakgrunn av skattekortet ditt.
                        Det kan være oppgitt som tabell- eller prosenttrekk.
                    </BodyLong>
                    <BodyLong spacing>
                        Tilleggstrekk av skatt er frivillig og kommer som et tillegg til det ordinære skattetrekket.
                    </BodyLong>
                    <BodyLong spacing>
                        Tilleggstrekket gjelder også i måneder det ikke trekkes skatt, eller skattetrekket er redusert.
                        For pensjoner gjelder dette desember (uten skattetrekk).
                        For uføretrygd gjelder det juni (uten skattetrekk), og desember (halvt skattetrekk).
                    </BodyLong>
                    <BodyLong spacing>
                        Frivillig skattetrekk du legger inn via Din pensjon eller Uføretrygd vil normalt løpe fra neste måned og ut året.
                        I enkelte tilfeller der trekket legges inn i slutten av måneden,
                        kan det gå én måned ekstra før trekket blir løpende.
                    </BodyLong>
                    <BodyLong spacing>
                        Eksempel: Hvis du har 5 000 kroner i ordinært skattetrekk og legger inn et tilleggstrekk på 1 000 kroner, vil det samlet bli
                        trukket 6 000 kroner i skatt per måned.
                    </BodyLong>
                </ReadMore>
            </div>

            <RegistrerteSkattetrekk skatteTrekk={skattetrekkLoader.skattetrekk} tilleggstrekk={skattetrekkLoader.tilleggstrekk} />


            <div id="skattetrekk-input">
                <Heading size={"medium"} level={"2"}>Legg til tilleggstrekk</Heading>
                <br/>
                <RadioGroup id="typeRadio"
                    legend="Hvordan skal skatten trekkes?"
                            size={"medium"}
                            description="skattetrekk per måned"
                            value={tilleggstrekkType}
                        >
                    <Radio value="prosent" onChange={(e) => {
                        settilleggstrekkType(e.target.value)
                    }}> Prosent </Radio>

                    <Radio value="kroner" onChange={(e) => {
                        settilleggstrekkType(e.target.value)
                    }}> Kroner </Radio>

                </RadioGroup>

                <div>
                    { tilleggstrekkType ?
                        <TextField id="tilleggstrekk_input"
                                   label= {"Hvor mange "  + tilleggstrekkType + "?"}
                                   value={tilleggstrekkValue}
                                   style={{width: "160px"}}
                                   onChange={onChangeValue} />  : <></>
                    }
                </div>

                <HStack gap="2">
                    <Button variant="primary" size={"medium"} loading={buttonIsLoading} onClick={submitTilleggstrekk}> Registrer </Button>
                    <Button variant="tertiary" size={"medium"} onClick={cancel}> Avbryt </Button>
                </HStack>
            </div>
        </>
    )
}