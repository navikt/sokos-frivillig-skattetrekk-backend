import {BodyShort, Heading, HGrid} from "@navikt/ds-react";
import {ForenkletSkattetrekk, SatsType, TrekkDTO} from "@/api/skattetrekkBackendClient";
import {numberFormatWithKr} from "@/common/Utils";
import "./RegistrerteSkattetrekk.css";


type RegistrerteSkattetrekkProps = {
    skatteTrekk: ForenkletSkattetrekk
    tilleggstrekk: TrekkDTO | null
}

export function RegistrerteSkattetrekk(props: RegistrerteSkattetrekkProps) {
    return (
        <HGrid gap="4" columns="min-content 1fr">
            <dt className="label"><strong>Trekk fra skattekortet:</strong></dt>
            <dd className="data">{showPercentageOrTable(props.skatteTrekk)}</dd>

            <dt className="label"><strong>Frivillig tilleggstrekk:</strong></dt>
            <dd className="data">{visProsentEllerBelop(props.tilleggstrekk)}</dd>
        </HGrid>
    )
}

function showPercentageOrTable(skattetrekk: ForenkletSkattetrekk) {
    if (skattetrekk.tabellNr != null) {
        return `Tabell ${skattetrekk.tabellNr}`
    } else if (skattetrekk.prosentsats != null) {
        return `${skattetrekk.prosentsats} %`
    }
}

function visProsentEllerBelop(tilleggstrekk: TrekkDTO | null) {
    if (tilleggstrekk == null) {
        return "Ingen tilleggstrekk"
    }

    if (tilleggstrekk.satsType == SatsType.PROSENT && tilleggstrekk.sats != null) {
        return `${tilleggstrekk.sats} %`
    } else if (tilleggstrekk.satsType == SatsType.KRONER && tilleggstrekk.sats != null) {
        return `${numberFormatWithKr(tilleggstrekk.sats)} per måned`
    }

    return "Ingen tilleggstrekk"
}
