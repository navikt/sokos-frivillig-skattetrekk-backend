import {Button, Heading, HStack, Radio, RadioGroup, TextField, VStack} from "@navikt/ds-react";
import {ChangeEvent, useCallback, useState} from "react";

export function Selector(props: {
    setType: (value: "prosent" | "kroner" | null) => void
    setValue: (value: number | null) => void
    submitTilleggstrekk: (type: "prosent" | "kroner", value: number | null) => void
}) {
    const [type, setType] = useState<"prosent" | "kroner" | null>(null)
    const [value, setValue] = useState<number | null>(null)
    const [buttonIsLoading, setButtonIsLoading] = useState(false)
    const [selectorError, setSelectorError] = useState(false)

    const onChangeType = (val: string) => {
            const value = val === "prosent" ? "prosent" : "kroner"
            setType(value);
            setSelectorError(false)
    }

    const onChangeValue = (val: ChangeEvent<HTMLInputElement>) => {
            const value = Number.parseInt(val.target.value);
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
