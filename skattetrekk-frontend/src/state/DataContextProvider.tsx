import {createContext, useCallback, useEffect, useState} from 'react'
import {fetchSkattetrekk, FrivilligSkattetrekkInitResponse} from "@/api/skattetrekkBackendClient";

interface DataContextValue {
    initiateResponse: FrivilligSkattetrekkInitResponse | null
    setInitiateResponse: (value: FrivilligSkattetrekkInitResponse) => void

    refetch: boolean
    setRefetch: (value: boolean) => void
}

const DataContextDefaultValue: DataContextValue = {
    initiateResponse: null,
    setInitiateResponse: () => undefined,

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

                refetch,
                setRefetch,
            }}>
            {props.children}
        </DataContext.Provider>
    )
}

export default DataContextProvider
