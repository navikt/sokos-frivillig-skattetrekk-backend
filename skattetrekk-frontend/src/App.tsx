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
            <VStack gap="14" className="contentWrapper">
                <Heading size={"xlarge"} level={"1"}>Frivillig skattetrekk</Heading>
                    <RouterProvider router={browserRouter}/>
            </VStack>
        </div>
    )
}

export default App


