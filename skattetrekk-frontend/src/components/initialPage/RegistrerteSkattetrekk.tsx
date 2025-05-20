import {Alert, BodyLong, HGrid, VStack} from "@navikt/ds-react";
import {ForenkletSkattetrekk, TrekkDTO} from "@/api/skattetrekkBackendClient";
import "./RegistrerteSkattetrekk.css";
import {showPercentageOrTable, visProsentEllerBelop} from "@/common/Utils";
import {useEffect} from "react";


type RegistrerteSkattetrekkProps = {
    skatteTrekk: ForenkletSkattetrekk
    tilleggstrekk: TrekkDTO | null
    framtidigTilleggstrekk: TrekkDTO | null;
}

export function RegistrerteSkattetrekk(props: RegistrerteSkattetrekkProps) {
    useEffect(() => {
        //log props
        console.log("RegistrerteSkattetrekk props: ", props)
    }, []);

    return (
        <VStack gap="4">
            {props.framtidigTilleggstrekk !== null &&
                <Alert variant="info">
                    <BodyLong>
                        {/*TODO date?*/}
                        Vi har registrert nytt frivillig skattetrekk på {visProsentEllerBelop(props.framtidigTilleggstrekk)} fra DATE
                        Det kan ta inntil 14 dager før trekket kommer med på utbetalingene dine. Trekket gjelder ut året.
                    </BodyLong>
                </Alert>
            }

            {props.tilleggstrekk !== null && props.framtidigTilleggstrekk?.sats === 0 && // TODO is this logic correct?
                <Alert variant="info">
                    <BodyLong>
                        Det frivillige skattetrekket er stoppet fra og med neste måned.
                    </BodyLong>
                </Alert>
            }

            <HGrid gap="4" columns="min-content 1fr">
                <dt className="label">
                    <strong>Frivillig tilleggstrekk:</strong>
                </dt>
                <dd className="data">{visProsentEllerBelop(props.tilleggstrekk)}</dd>

                <dt className="label"><strong>Trekk fra skattekortet:</strong></dt>
                <dd className="data">{showPercentageOrTable(props.skatteTrekk)}</dd>
            </HGrid>
        </VStack>)
}


