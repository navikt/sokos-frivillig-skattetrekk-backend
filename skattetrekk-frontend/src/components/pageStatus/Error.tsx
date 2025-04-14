import {Alert} from "@navikt/ds-react";
import "./Error.css"

export function Error() {

    return (
        <>
            <div id="error-div">
            <Alert variant="error">Noe gikk galt! Prøv igjen om noen minutter.</Alert>
            </div>
        </>
    )
}