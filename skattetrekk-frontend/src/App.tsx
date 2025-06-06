import './App.css'
import "@navikt/ds-css";
import {Heading, VStack} from "@navikt/ds-react";
import {PageLinks, routes} from "@/routes";
import {createBrowserRouter, NavigateFunction, RouterProvider, useNavigate} from "react-router-dom";
import {Suspense, useEffect} from "react";
import {Loading} from "@/components/pageStatus/Loading";
import DataContextProvider from "@/state/DataContextProvider";


const browserRouter = createBrowserRouter(routes)

export function App() {


    return (
        <div className="mainBody">
            <VStack gap="14" className="contentWrapper">
                <Heading size={"xlarge"} level={"1"}>Frivillig skattetrekk</Heading>
                <DataContextProvider>
                    <RouterProvider router={browserRouter}/>
                </DataContextProvider>
            </VStack>
        </div>
    )
}

export default App


