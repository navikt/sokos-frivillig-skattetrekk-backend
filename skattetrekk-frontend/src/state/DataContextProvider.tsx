import {createContext, useCallback, useEffect, useState} from 'react'
import {fetchSkattetrekk, FrivilligSkattetrekkData, FrivilligSkattetrekkResponse} from "@/api/skattetrekkBackendClient";

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

function DataContextProvider(props: DataContextProviderProps) {
    const [shouldRefetch, setShouldRefetch] = useState(true)
    const [initiateResponse, setInitiateResponse] = useState(DataContextDefaultValue.initiateResponse)
    const [sendResponse, setSendResponse] = useState(DataContextDefaultValue.sendResponse)

    const refetch = useCallback(
        async ()  => {
            setShouldRefetch(false)
            try {
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
            {props.children}
        </DataContext.Provider>
    )
}

export default DataContextProvider
