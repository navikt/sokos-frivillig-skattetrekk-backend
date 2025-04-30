import {InitialPage} from "@/components/initial/InitialPage";
import {Outlet, RouteObject, useLocation} from "react-router-dom";
import {initSkattetrekkLoader} from "@/loaders/initSkattetrekkLoader";
import {Error} from "@/components/pageStatus/Error";
import {KvitteringPage} from "@/components/kvittering/KvitteringPage";
import {useEffect} from "react";

const ScrollToTop = () => {
    const {pathname} = useLocation()
    useEffect(() => {
        window.scrollTo(0, 0)
    }, [pathname])
    return <Outlet />
}

export const routes: RouteObject[] = [
    {
        element: <ScrollToTop/>,
        children: [
            {
                path: import.meta.env.BASE_URL,
                element: <InitialPage/>,
                loader: initSkattetrekkLoader,
                errorElement: <Error/>
            },
            {
                path: import.meta.env.BASE_URL + "/kvittering",
                element: <KvitteringPage/>,
                errorElement: <Error/>
            }
        ]
    }

]