import {BodyShort, Heading} from "@navikt/ds-react";
import {ForenkletSkattetrekk, SatsType, TrekkDTO} from "@/api/skattetrekkBackendClient";


type RegistrerteSkattetrekkProps = {
    skatteTrekk: ForenkletSkattetrekk
    tilleggstrekk: TrekkDTO | null
}

export function RegistrerteSkattetrekk(props: RegistrerteSkattetrekkProps) {
    return (
        <div>
            <BodyShort spacing>
                <strong>Trekk fra skattekortet:</strong> &emsp; {showPercentageOrTable(props.skatteTrekk)}
            </BodyShort>

            <BodyShort spacing>
                <strong>Frivillig tilleggstrekk:</strong> &emsp; {visProsentEllerBelop(props.tilleggstrekk)}
            </BodyShort>
        </div>
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
        return `${tilleggstrekk.sats} kr per måned`
    }

    return "Ingen tilleggstrekk"
}
