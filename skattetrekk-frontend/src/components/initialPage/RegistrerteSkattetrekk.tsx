import {BodyLong, HGrid} from "@navikt/ds-react";
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
        <HGrid gap="4" columns="min-content 1fr">
            <dt className="label">
                <strong>{props.tilleggstrekk ? "Frivillig skattetrekk til og med nåværende måned:" : "Frivillig tilleggstrekk:"} </strong>
            </dt>
            <dd className="data">{visProsentEllerBelop(props.tilleggstrekk)}</dd>

            <dt className="label"><strong>Trekk fra skattekortet {new Date().getFullYear()}:</strong></dt>
            <dd className="data">{showPercentageOrTable(props.skatteTrekk)}</dd>

            {props.framtidigTilleggstrekk != null &&
                <dt className="label"><strong>Frivillig skattetrekk fra og med neste måned:</strong></dt>}
            {props.framtidigTilleggstrekk != null &&
                <dd className="data">{visProsentEllerBelop(props.framtidigTilleggstrekk)}</dd>}
        </HGrid>)
}


