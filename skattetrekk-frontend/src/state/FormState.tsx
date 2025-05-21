import React, {createContext, SetStateAction, useState} from 'react'
import {SatsType} from "@/api/skattetrekkBackendClient";

interface FormState {
    isEligible: boolean | null
    setIsEligible: (value: SetStateAction<boolean | null>) => void
    
    tilleggstrekkType: SatsType | null
    setTilleggstrekkType: (value: SetStateAction<SatsType | null>) => void

    tilleggstrekkValue: number | null
    setTilleggstrekkValue: (value: SetStateAction<number | null>) => void
}

export const FormStateContext = createContext<FormState>({
    isEligible: null,
    setIsEligible: () => undefined,

    tilleggstrekkType: null,
    setTilleggstrekkType: () => undefined,

    tilleggstrekkValue: null,
    setTilleggstrekkValue: () => undefined
})

interface Props {
    children: React.ReactNode
}

export function  resetFormState() {
    return {
        isEligible: null,
        tilleggstrekkType: null,
        tilleggstrekkValue: null
    }
}

export const FormStateComponent = ({ children }: Props) => {
    const [isEligible, setIsEligible] = useState<boolean | null>(null)
    const [tilleggstrekkType, setTilleggstrekkType] = useState<SatsType | null>(null)
    const [tilleggstrekkValue, setTilleggstrekkValue] = useState<number | null>(null)
    

    return (
        <FormStateContext.Provider
            value={{
                isEligible,
                setIsEligible,

                tilleggstrekkType,
                setTilleggstrekkType,

                tilleggstrekkValue,
                setTilleggstrekkValue,

            }}>
            {children}
        </FormStateContext.Provider>
    )
}
