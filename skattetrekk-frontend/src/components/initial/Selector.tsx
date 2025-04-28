import {Button, Heading, HStack, Radio, RadioGroup, TextField, VStack} from "@navikt/ds-react";
import {ChangeEvent, useCallback, useState} from "react";
import {SatsType} from "@/api/skattetrekkBackendClient";

export function Selector(props: {
    setType: (value: SatsType | null) => void
    setValue: (value: number | null) => void
    submitTilleggstrekk: (type: SatsType, value: number | null) => void
}) {
    const [type, setType] = useState<SatsType | null>(null)
    const [value, setValue] = useState<number | null>(null)
    const [buttonIsLoading, setButtonIsLoading] = useState(false)
    const [selectorError, setSelectorError] = useState(false)
    const [amountError, setAmountError] = useState("")

    const onChangeType = (val: SatsType) => {
            setType(val);
            setSelectorError(false)
    }

    const onChangeValue = (val: ChangeEvent<HTMLInputElement>) => {
            const value = Number.parseInt(val.target.value);
            if(type === SatsType.PROSENT) {
                if (value > 100) {
                    setAmountError("Du kan maks oppgi 100%")
                }
            } else if(SatsType.KRONER) {
                if (value < 0) {
                    setAmountError("Beløp må være større enn 0")
                } else if (value > 50000) {
                    setAmountError("Du kan maks oppgi 50 000 kr")
                }
            }

            setValue(Number.isNaN(value) ? null : value);
    }

    function onClickSubmit(event: React.FormEvent) {
        event.preventDefault()

        if(type == null) {
            setSelectorError(true)
            return
        }

        if(value != null) {
            setButtonIsLoading(true)
            props.submitTilleggstrekk(type, value)
            setButtonIsLoading(false)
        }
    }


    return (
      <VStack gap="4" id="skattetrekk-input">
          <Heading size={"medium"} level={"2"}>Legg til tilleggstrekk</Heading>
          <RadioGroup id="typeRadio"
                      legend="Hvordan skal skatten trekkes?"
                      size={"medium"}
                      description="skattetrekk per måned"
                      value={type}
                      onChange={onChangeType}
                      error={selectorError ? "Du må velge en type" : undefined}>

              <Radio value="prosent">Prosent på alle skattepliktige ytelser/pengestøtter</Radio>
              <Radio value="kroner">Kroner på hver måneds første  utbetaling av skattepliktig ytelse/pengestøtte </Radio>
          </RadioGroup>

          {type != null ?
              <TextField id="tilleggstrekk_input"
                         label={"Hvor mange " + type + "?"}
                         style={{width: "160px"}}
                         onChange={onChangeValue}/> : null
          }

          <HStack gap="2">
              <Button variant="primary" size={"medium"} loading={buttonIsLoading} type={"submit"}
                      onClick={onClickSubmit}> Registrer </Button>
              <Button variant="tertiary" size={"medium"}> Avbryt </Button>
          </HStack>
      </VStack>
    )
}
