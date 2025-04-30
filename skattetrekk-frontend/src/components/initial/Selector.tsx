import {Button, Heading, HStack, Radio, RadioGroup, TextField, VStack} from "@navikt/ds-react";
import {useState} from "react";
import {SatsType} from "@/api/skattetrekkBackendClient";
import {parseInntekt} from "@/util/NumberUtil";
import {numberFormatWithKr} from "@/common/Utils";

export function Selector(props: {
    maxKroner: number
    submitTilleggstrekk: (type: SatsType, value: number | null) => void
    buttonIsLoading: boolean
}) {
    const [type, setType] = useState<SatsType | null>(null)
    const [value, setValue] = useState<number | null>(null)
    const [valueError, setValueError] = useState<string | null>(null)
    const [selectorError, setSelectorError] = useState(false)

    const onChangeType = (val: SatsType) => {
            setType(val);
            setSelectorError(false)
    }

    const handleChangeValue = (val: string | null, typeVal: SatsType) => {
        if (val === '' || val === null) {
            setValueError(null)
            setValue(null)
            return
        }

        const numericValue = parseInntekt(val)

        if (isNaN(numericValue) || numericValue < 0) {
            setValueError('Du kan ikke skrive mellomrom, bokstaver eller tegn')
        } else if (typeVal === SatsType.PROSENT && numericValue > 100) {
            setValueError('Du kan maks oppgi 100 %')
        } else if (typeVal === SatsType.KRONER && numericValue > props.maxKroner) {
            setValueError(`Du kan maks oppgi ${numberFormatWithKr(props.maxKroner)}. Vil du trekke et høyere beløp, kan du legge det inn som prosent`)
        } else if (typeVal === SatsType.KRONER && numericValue === 0) {
            setValueError('Du må oppgi et høyere beløp enn 0 kr')
        }

        else {
            setValueError(null)
            setValue(numericValue)
        }
    }

    function onClickSubmit(event: React.FormEvent) {
        event.preventDefault()

        if (type == null) {
            setSelectorError(true)
            return
        }

        if (valueError === null) {
            props.submitTilleggstrekk(type, value)
        }
    }


    return (
        <VStack gap="4" id="skattetrekk-input">
            <Heading size={"medium"} level={"2"}>Legg til frivillig tilleggstrekk</Heading>
            <RadioGroup id="typeRadio"
                        legend="Hvordan skal skatten trekkes?"
                        size={"medium"}
                        description="Skattetrekk per måned"
                        value={type}
                        onChange={(v) => {
                            onChangeType(v);
                            handleChangeValue((document.getElementById('tilleggstrekk_input') as HTMLInputElement).value, v)
                        }}

                        error={selectorError ? "Du må velge hvilken type frivillig skattetrekk du ønsker" : undefined}>

                <Radio value={SatsType.PROSENT}>Prosent på alle skattepliktige ytelser/pengestøtter</Radio>
                <Radio value={SatsType.KRONER}>Kroner på hver måneds første utbetaling av skattepliktig ytelse/pengestøtte </Radio>
            </RadioGroup>

            {type != null ?
                <TextField id="tilleggstrekk_input"
                           label={type === SatsType.PROSENT ? "Hvor mange prosent?" : "Hvor mange kroner?"}
                           style={{width: "160px"}}
                           inputMode="numeric"
                           error={valueError}
                           pattern="[\d\s]+"
                           onBlur={(v => handleChangeValue(v.target.value, type))}
                           onChange={(v => handleChangeValue(v.target.value, type))}
                           htmlSize={30}
                /> : null
            }

            <HStack gap="2">
                <Button variant="primary" size={"medium"} loading={props.buttonIsLoading} type={"submit"}
                        onClick={onClickSubmit}> Registrer </Button>
                <Button variant="tertiary" size={"medium"}> Avbryt </Button>
            </HStack>
        </VStack>
    )
}
