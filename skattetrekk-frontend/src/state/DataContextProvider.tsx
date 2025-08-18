import React, {createContext, useCallback, useEffect, useState} from 'react'
import {fetchSkattetrekk, FrivilligSkattetrekkResponse} from "@/api/skattetrekkBackendClient";
import {BodyShort, Box, Loader, VStack} from "@navikt/ds-react";
import './DataContextProvider.css'

interface DataContextValue {
    getResponse: FrivilligSkattetrekkResponse | null
    setGetResponse: (value: FrivilligSkattetrekkResponse) => void
    setShouldRefetch: (value: boolean) => void,
    setLoaderOverride: (value: boolean) => void,
    getLoaderOverride: boolean
}

const DataContextDefaultValue: DataContextValue = {
    getResponse: null,
    setGetResponse: () => undefined,
    setShouldRefetch: () => undefined,
    setLoaderOverride: () => undefined,
    getLoaderOverride: false
}

export const DataContext = createContext(DataContextDefaultValue)

interface DataContextProviderProps {
    children?: React.ReactNode
}

function DataContextProvider(props: DataContextProviderProps) {
    const [isFetching, setIsFetching] = useState(false)
    const [shouldRefetch, setShouldRefetch] = useState(true)
    const [getResponse, setGetResponse] = useState(DataContextDefaultValue.getResponse)
    const [loaderOverride, setLoaderOverride] = useState(false)

    const refetch = useCallback(
        async ()  => {
            setIsFetching(true)
            setShouldRefetch(false)
            try {
                const response = await fetchSkattetrekk()
                setGetResponse(response)
            } catch (error) {
                setShouldRefetch(true) // Reset shouldRefetch to true since the refetch failed
            } finally {
                setIsFetching(false)
                setLoaderOverride(false)
            }
        },
        [setShouldRefetch]
    )

    useEffect(() => {
        if (shouldRefetch) {
            refetch()
        }
    }, [refetch, shouldRefetch])

    const showLoader = (getResponse === null || isFetching) && !loaderOverride;

    return (
        <DataContext.Provider
            value={{
                getResponse,
                setGetResponse,
                setShouldRefetch,
                setLoaderOverride,
                getLoaderOverride: loaderOverride
            }}>
            <Box position="relative" minHeight={showLoader ? "400px" : undefined}>
                {props.children}

                {(showLoader) ?
                    (
                        <Box id="data-context-overlay" position="absolute" left="0" right="0" top="0" bottom="0" background="bg-default">
                            <Box background="bg-subtle" padding="16" borderRadius="large">
                                <VStack align="center" gap="20">
                                    <Loader size="3xlarge" />
                                    <BodyShort align="center">{"Vent mens vi laster inn siden."}</BodyShort>
                                </VStack>
                            </Box>
                        </Box>
                    ) : null}
            </Box>
        </DataContext.Provider>
    )
}

export default DataContextProvider
