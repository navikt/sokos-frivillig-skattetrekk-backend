import {Loader} from "@navikt/ds-react";
import "./Loading.css"

export function Loading() {
    return (<>
        <div id="loading-holder">
            <div id="loading">
                <Loader size="3xlarge" title="Venter..." />
            </div>
        </div>
    </>)

}