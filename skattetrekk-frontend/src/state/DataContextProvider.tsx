import {createContext, useCallback, useEffect, useState} from 'react'
import {fetchSkattetrekk, FrivilligSkattetrekkData} from "@/api/skattetrekkBackendClient";

interface DataContextValue {
    initiateResponse: FrivilligSkattetrekkData | null
    setInitiateResponse: (value: FrivilligSkattetrekkData) => void

    sendResponse: FrivilligSkattetrekkData | null
    setSendResponse: (value: FrivilligSkattetrekkData) => void

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
                setInitiateResponse(response.data)
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
