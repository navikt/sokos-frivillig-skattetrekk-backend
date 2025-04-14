import './App.css'
import "@navikt/ds-css";
import {Heading} from "@navikt/ds-react";
import {routes} from "@/routes";
import {createBrowserRouter, RouterProvider} from "react-router-dom";
import {Suspense} from "react";
import {Loading} from "@/components/pageStatus/Loading";


const browserRouter = createBrowserRouter(routes)

export function App() {

    return (
        <div className="mainBody">
            <div className="contentWrapper">
                <Heading size={"xlarge"} level={"1"}>Frivillig skattetrekk</Heading>
                <Suspense fallback={<Loading/>}>
                    <RouterProvider router={browserRouter}/>
                </Suspense>
            </div>
        </div>
    )
}

export default App
