import {InitialPage} from "@/components/initialPage/InitialPage";
import {Outlet, RouteObject, useLocation} from "react-router-dom";
import {initSkattetrekkLoader} from "@/loaders/initSkattetrekkLoader";
import {Error} from "@/components/pageStatus/Error";
import {KvitteringPage} from "@/components/kvittering/KvitteringPage";
import {useEffect} from "react";
import {OppsummeringPage} from "@/components/oppsummeringPage/OppsummeringPage";
import {FormPage} from "@/components/formPage/FormPage";

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
                loader: initSkattetrekkLoader,
                errorElement: <Error/>
            },
            {
                path: import.meta.env.BASE_URL + PageLinks.ENDRING,
                element: <FormPage/>,
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

export const getFullPathForPage = (pageLink: PageLinks, href?: string) => {
    return import.meta.env.BASE_URL + pageLink + (href ? `#${href}` : "")  + getPidQueryParamString()
}

export const getPidQueryParamString = () => {
    const searchParams = new URLSearchParams(document.location.search)
    const pid = searchParams.get('pid')
    if (pid === null) {
        return ''
    }
    return '?pid=' + pid
}

