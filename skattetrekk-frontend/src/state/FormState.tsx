import React, {createContext, SetStateAction, useState} from 'react'

interface FormState {
    tilleggstrekkType: "prosent" | "kroner" | null
    setTilleggstrekkType: (value: SetStateAction<"prosent" | "kroner" | null>) => void

    tilleggstrekkValue: number | null
    setTilleggstrekkValue: (value: SetStateAction<number | null>) => void
}

export const FormStateContext = createContext<FormState>({
    tilleggstrekkType: null,
    setTilleggstrekkType: () => undefined,

    tilleggstrekkValue: null,
    setTilleggstrekkValue: () => undefined,
})

interface Props {
    children: React.ReactNode
}

export const FormStateComponent = ({ children }: Props) => {
    const [tilleggstrekkType, setTilleggstrekkType] = useState<"prosent" | "kroner" | null>(null)
    const [tilleggstrekkValue, setTilleggstrekkValue] = useState<number | null>(null)

    return (
        <FormStateContext.Provider
            value={{
                tilleggstrekkType,
                setTilleggstrekkType,

                tilleggstrekkValue,
                setTilleggstrekkValue,

            }}>
            {children}
        </FormStateContext.Provider>
    )
}
