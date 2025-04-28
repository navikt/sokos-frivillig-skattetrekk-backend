import {InitialPage} from "@/components/initial/InitialPage";
import {RouteObject} from "react-router-dom";
import {initSkattetrekkLoader} from "@/loaders/initSkattetrekkLoader";
import {Error} from "@/components/pageStatus/Error";
import {KvitteringPage} from "@/components/kvittering/KvitteringPage";

export const routes: RouteObject[] = [
    {
        path: import.meta.env.BASE_URL,
        element: <InitialPage/>,
        loader: initSkattetrekkLoader,
        errorElement: <Error/>
    },
    {
        path: import.meta.env.BASE_URL + "/kvittering",
        element: <KvitteringPage/>,
        loader: initSkattetrekkLoader,
        errorElement: <Error/>
    }
]