import React, {createContext, useCallback, useEffect, useState} from 'react'
import {fetchSkattetrekk, FrivilligSkattetrekkData, FrivilligSkattetrekkResponse} from "@/api/skattetrekkBackendClient";
import {BodyShort, Box, Loader, VStack} from "@navikt/ds-react";

interface DataContextValue {
    initiateResponse: FrivilligSkattetrekkResponse | null
    setInitiateResponse: (value: FrivilligSkattetrekkResponse) => void

    sendResponse: FrivilligSkattetrekkResponse | null
    setSendResponse: (value: FrivilligSkattetrekkResponse) => void
}

const DataContextDefaultValue: DataContextValue = {
    initiateResponse: null,
    setInitiateResponse: () => undefined,

    sendResponse: null,
    setSendResponse: () => undefined,
}

export const DataContext = createContext(DataContextDefaultValue)

interface DataContextProviderProps {
    children?: React.ReactNode
}

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

function DataContextProvider(props: DataContextProviderProps) {
    const [shouldRefetch, setShouldRefetch] = useState(true)
    const [initiateResponse, setInitiateResponse] = useState(DataContextDefaultValue.initiateResponse)
    const [sendResponse, setSendResponse] = useState(DataContextDefaultValue.sendResponse)

    const refetch = useCallback(
        async ()  => {
            setShouldRefetch(false)
            try {
                await delay(1000)
                const response = await fetchSkattetrekk()
                setInitiateResponse(response)
            } catch (error) {
                setShouldRefetch(true) // Reset shouldRefetch to true since the refetch failed
            }

        },
        [setShouldRefetch]
    )

    useEffect(() => {
        if (shouldRefetch) {
            refetch() // Call refetch every time shouldRefetch is set to true.
        }
    }, [refetch, shouldRefetch])

    return (
        <DataContext.Provider
            value={{
                initiateResponse,
                setInitiateResponse,

                sendResponse,
                setSendResponse
            }}>
            {(initiateResponse === null) ?
                (
                    <Box background="bg-subtle" padding="16" borderRadius="large">
                        <VStack align="center" gap="20">
                            <Loader size="3xlarge" />
                            <BodyShort align="center">{"Vent mens vi laster inn siden."}</BodyShort>
                        </VStack>
                    </Box>
                ) : props.children}
        </DataContext.Provider>
    )
}

export default DataContextProvider
