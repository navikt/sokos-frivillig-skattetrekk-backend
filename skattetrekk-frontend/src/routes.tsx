import {InitialPage} from "@/components/initialPage/InitialPage";
import {Outlet, RouteObject, useLocation} from "react-router-dom";
import {initSkattetrekkLoader} from "@/loaders/initSkattetrekkLoader";
import {Error} from "@/components/pageStatus/Error";
import {KvitteringPage} from "@/components/kvittering/KvitteringPage";
import {useEffect} from "react";
import {OppsummeringPage} from "@/components/oppsummeringPage/OppsummeringPage";
import {EndringPage} from "@/components/endringPage/EndringPage";

const ScrollToTop = () => {
    const {pathname} = useLocation()
    useEffect(() => {
        window.scrollTo(0, 0)
    }, [pathname])
    return <Outlet />
}

export enum PageLinks {
    INDEX = '/',
    ENDRING = '/endring',
    KVITTERING = '/kvittering',
    OPPSUMMERING = '/oppsummering',
}

export const routes: RouteObject[] = [
    {
        element: <ScrollToTop/>,
        children: [
            {
                path: import.meta.env.BASE_URL,
                element: <InitialPage/>,
                errorElement: <Error/>
            },
            {
                path: import.meta.env.BASE_URL + PageLinks.ENDRING,
                element: <EndringPage/>,
                errorElement: <Error/>
            },
            {
                path: import.meta.env.BASE_URL + PageLinks.OPPSUMMERING,
                element: <OppsummeringPage/>,
                errorElement: <Error/>
            },
            {
                path: import.meta.env.BASE_URL + PageLinks.KVITTERING,
                element: <KvitteringPage/>,
                errorElement: <Error/>
            },

        ]
    }
]
