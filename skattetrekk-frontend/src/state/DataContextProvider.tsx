import {createContext, useCallback, useEffect, useState} from 'react'
import {fetchSkattetrekk, FrivilligSkattetrekkData, FrivilligSkattetrekkResponse} from "@/api/skattetrekkBackendClient";

interface DataContextValue {
    initiateResponse: FrivilligSkattetrekkResponse | null
    setInitiateResponse: (value: FrivilligSkattetrekkResponse) => void

    sendResponse: FrivilligSkattetrekkResponse | null
    setSendResponse: (value: FrivilligSkattetrekkResponse) => void

    refetch: boolean
    setRefetch: (value: boolean) => void
}

const DataContextDefaultValue: DataContextValue = {
    initiateResponse: null,
    setInitiateResponse: () => undefined,

    sendResponse: null,
    setSendResponse: () => undefined,

    refetch: true,
    setRefetch: () => undefined,
}

export const DataContext = createContext(DataContextDefaultValue)

interface DataContextProviderProps {
    children?: React.ReactNode
}

function DataContextProvider(props: DataContextProviderProps) {
    const [refetch, setRefetch] = useState(DataContextDefaultValue.refetch)
    const [initiateResponse, setInitiateResponse] = useState(DataContextDefaultValue.initiateResponse)
    const [sendResponse, setSendResponse] = useState(DataContextDefaultValue.sendResponse)

    useCallback(
        function (res: boolean) {
            setRefetch(res)
        },
        [setRefetch]
    )

    useEffect(() => {
        (async () => {
            if (refetch) {
                const response = await fetchSkattetrekk()
                setInitiateResponse(response)
                setRefetch(false)
            }
        })()
    }, [refetch])

    return (
        <DataContext.Provider
            value={{
                initiateResponse,
                setInitiateResponse,

                sendResponse,
                setSendResponse,

                refetch,
                setRefetch,
            }}>
            {props.children}
        </DataContext.Provider>
    )
}

export default DataContextProvider
