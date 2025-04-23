import {Button, Heading, HStack, Radio, RadioGroup, TextField, VStack} from "@navikt/ds-react";
import {ChangeEvent, useCallback, useState} from "react";

export function Selector(props: {
    setType: (value: "prosent" | "kroner" | null) => void
    setValue: (value: number | null) => void
}) {
    const [type, setType] = useState<"prosent" | "kroner" | null>(null)
    const [value, setValue] = useState<number | null>(null)

    const onChangeType = (val: string) => {
            const value = val === "prosent" ? "prosent" : "kroner"
            setType(value);
    }

    const onChangeValue = (val: ChangeEvent<HTMLInputElement>) => {
            const value = Number.parseInt(val.target.value);
            setValue(Number.isNaN(value) ? null : value);
    }


    return (
      <VStack id="skattetrekk-input">
          <Heading size={"medium"} level={"2"}>Legg til tilleggstrekk</Heading>
          <br/>
          <RadioGroup id="typeRadio"
                      legend="Hvordan skal skatten trekkes?"
                      size={"medium"}
                      description="skattetrekk per måned"
                      value={type}
                      onChange={onChangeType}>

              <Radio value="prosent"> Prosent </Radio>
              <Radio value="kroner"> Kroner </Radio>
          </RadioGroup>

          {type != null ?
              <TextField id="tilleggstrekk_input"
                         label={"Hvor mange " + type + "?"}
                         style={{width: "160px"}}
                         onChange={onChangeValue}/> : null
          }
      </VStack>
    )
}
