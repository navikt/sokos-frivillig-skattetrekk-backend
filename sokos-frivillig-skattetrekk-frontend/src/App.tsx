import './App.css'
import "@navikt/ds-css";
import {Heading, VStack} from "@navikt/ds-react";
import {routes} from "@/routes";
import {createBrowserRouter, RouterProvider} from "react-router-dom";
import DataContextProvider from "@/state/DataContextProvider";

const browserRouter = createBrowserRouter(routes)

export function App() {

    return (
        <div className="mainBody">
            <main id="maincontent" tabIndex={-1}>
            <VStack gap="14" className="contentWrapper" >
                <Heading size={"xlarge"} level={"1"}>Frivillig skattetrekk</Heading>
                <DataContextProvider>
                    <RouterProvider router={browserRouter}/>
                </DataContextProvider>
            </VStack>
            </main>
        </div>
    )
}

export default App


