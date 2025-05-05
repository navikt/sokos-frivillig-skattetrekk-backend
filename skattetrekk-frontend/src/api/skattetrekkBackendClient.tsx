export interface TrekkDTO {
    trekkvedtakId: string | null;
    sats: number | null;
    satsType: SatsType | null;
}

export interface ForenkletSkattetrekk {
    trekkvedtakId: string | null;
    tabellNr: string | null;
    prosentsats: number | null;
}

export enum SatsType {
    PROSENT = "PROSENT",
    KRONER = "KRONER"
}

export interface FrivilligSkattetrekkInitResponse {
    tilleggstrekk: TrekkDTO | null;
    framtidigTilleggstrekk: TrekkDTO | null;
    skattetrekk: ForenkletSkattetrekk;
}

export interface SaveRequest{
    trekkVedtakId: string;
    value: number;
    satsType: SatsType
}

const isMock = process.env.isMock || false
const PORT = process.env.MOCK_PORT || "8080"
const BASE_URL = isMock ? "http://" + window.location.hostname + ":" + PORT + import.meta.env.BASE_URL + "/"
    : import.meta.env.BASE_URL + "/"

export async function fetchSkattetrekk(): Promise<FrivilligSkattetrekkInitResponse> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid = searchParams.get("pid")

    let headers;
    if (pid !== null) {
        headers =  {
            'Content-Type': 'application/json',
            'pid': pid,
        }
    } else {
        headers = {
            'Content-Type': 'application/json',
        }
    }

    if(isMock) {
        return {
            tilleggstrekk:
                {
                    trekkvedtakId: "123",
                    sats: 200,
                    satsType: SatsType.KRONER
                },
            framtidigTilleggstrekk: null,
            skattetrekk: {
                trekkvedtakId: "123",
                tabellNr: null, //"800",
                prosentsats: 25
            }
        }
    }

    return await fetch(BASE_URL+ "api/skattetrekk", {
            method: "GET",
            credentials: "include",
            headers: headers
        }
    ).then(
        response => {
            if (response.status >= 200 && response.status < 300) {
                return response.json().then(data => {
                    return data as FrivilligSkattetrekkInitResponse;
                })
            } else if (response.status == 400) {
                return response.json().then(
                    data => {
                        return data.feilkode
                    }
                )
            } else {
                throw new Error("Fikk ikke 2xx respons fra server");
            }
        }
    )
}

export async function saveSkattetrekk(request: SaveRequest): Promise<boolean> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid = searchParams.get("pid")

    let headers;
    if (pid !== null) {
        headers =  {
            'Content-Type': 'application/json',
            'pid': pid,
        }
    } else {
        headers = {
            'Content-Type': 'application/json',
        }
    }

    console.log("saveSkattetrekk", request)

    if(isMock) {
        return true
    }

    return await fetch(BASE_URL+ "api/skattetrekk", {
            method: "POST",
            credentials: "include",
            headers: headers
        }
    ).then(
        response => {
            if (response.status >= 200 && response.status < 300) {
                return true
            } else {
                return false
            }
        }
    )
}

