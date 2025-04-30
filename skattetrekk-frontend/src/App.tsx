import './App.css'
import "@navikt/ds-css";
import {Heading, VStack} from "@navikt/ds-react";
import {routes} from "@/routes";
import {createBrowserRouter, RouterProvider, useLocation} from "react-router-dom";
import {Suspense, useEffect} from "react";
import {Loading} from "@/components/pageStatus/Loading";
import {FormStateComponent} from "@/state/FormState";


const browserRouter = createBrowserRouter(routes)

export function App() {


    return (
        <div className="mainBody">
            <VStack gap="6" className="contentWrapper">
                <Heading size={"xlarge"} level={"1"}>Frivillig skattetrekk</Heading>
                <Suspense fallback={<Loading/>}>
                    <RouterProvider router={browserRouter}/>
                </Suspense>
            </VStack>
        </div>
    )
}

export default App
