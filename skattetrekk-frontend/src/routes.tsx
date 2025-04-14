import {FrivilligSkattetrekkView} from "@/components/frivilligSkattetrekkView/FrivilligSkattetrekkView";
import {RouteObject} from "react-router-dom";
import {initSkattetrekkLoader} from "@/loaders/initSkattetrekkLoader";
import {Error} from "@/components/pageStatus/Error";

export const routes: RouteObject[] = [
    {
        path: import.meta.env.BASE_URL,
        element: <FrivilligSkattetrekkView/>,
        loader: initSkattetrekkLoader,
        errorElement: <Error/>
    }
]