import {BodyShort, Heading} from "@navikt/ds-react";
import {ForenkletSkattetrekk, SatsType, TrekkDTO} from "@/api/skattetrekkBackendClient";


type RegistrerteSkattetrekkProps = {
    skatteTrekk: ForenkletSkattetrekk
    tilleggstrekk: TrekkDTO | null
}

export function RegistrerteSkattetrekk(props: RegistrerteSkattetrekkProps) {
    return (
        <div>
            <Heading size="medium" level="2" spacing>Registrerte skattetrekk</Heading>

            <BodyShort spacing>
                Trekk fra skattekortet: &emsp; {visProsentEllerTabell(props.skatteTrekk)}
            </BodyShort>

            <BodyShort spacing>
                Frivillig tilleggstrekk: &emsp; {visProsentEllerBelop(props.tilleggstrekk)}
            </BodyShort>
        </div>
    )
}

function visProsentEllerTabell(skattetrekk: ForenkletSkattetrekk) {
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
