import {InitialPage} from "@/components/initial/InitialPage";
import {RouteObject} from "react-router-dom";
import {initSkattetrekkLoader} from "@/loaders/initSkattetrekkLoader";
import {Error} from "@/components/pageStatus/Error";

export const routes: RouteObject[] = [
    {
        path: import.meta.env.BASE_URL,
        element: <InitialPage/>,
        loader: initSkattetrekkLoader,
        errorElement: <Error/>
    }
]