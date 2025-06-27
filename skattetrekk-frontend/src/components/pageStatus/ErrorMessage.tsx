import {Alert} from "@navikt/ds-react";
import "./Error.css"

export function ErrorMessage() {

    return (
        <>
            <div id="error-div">
            <Alert variant="error">Det har skjedd en teknisk feil. Hvis du har registrert informasjon, har den dessverre ikke blitt lagret. Vi beklager for dette. Du kan prøve igjen senere.
                Ta gjerne kontakt med oss hvis problemet fortsetter.</Alert>
            </div>
        </>
    )
}