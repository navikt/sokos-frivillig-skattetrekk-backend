import {HGrid} from "@navikt/ds-react";
import {ForenkletSkattetrekk, TrekkDTO} from "@/api/skattetrekkBackendClient";
import "./RegistrerteSkattetrekk.css";
import {showPercentageOrTable, visProsentEllerBelop} from "@/common/Utils";


type RegistrerteSkattetrekkProps = {
    skatteTrekk: ForenkletSkattetrekk
    tilleggstrekk: TrekkDTO | null
    framtidigTilleggstrekk: TrekkDTO | null;
}

export function RegistrerteSkattetrekk(props: RegistrerteSkattetrekkProps) {
    return (
        <HGrid gap="4" columns="min-content 1fr">
            <dt className="label"><strong>Trekk fra skattekortet:</strong></dt>
            <dd className="data">{showPercentageOrTable(props.skatteTrekk)}</dd>

            <dt className="label"><strong>{props.framtidigTilleggstrekk ?  "Frivillig skattetrekk til og med nåværende måned:" : "Frivillig tilleggstrekk:"} </strong></dt>
            <dd className="data">{visProsentEllerBelop(props.tilleggstrekk)}</dd>

            {props.framtidigTilleggstrekk != null && <dt className="label"><strong>Frivillig skattetrekk fra og med neste måned:</strong></dt> }
            {props.framtidigTilleggstrekk != null && <dd className="data">{visProsentEllerBelop(props.framtidigTilleggstrekk)}</dd> }
    </HGrid>)
}


