export interface TrekkDTO {
    sats: number | null;
    satsType: SatsType | null;
}

export interface ForenkletSkattetrekk {
    tabellNr: string | null;
    prosentsats: number | null;
}

export enum SatsType {
    PROSENT = "PROSENT",
    KRONER = "KRONER"
}

export interface Message {
    details: string | null,
    type: MessageType,
    code: string | null,
}

export enum MessageType {
    ERROR,
    WARNING,
    INFO
}

export interface FrivilligSkattetrekkResponse {
    data: FrivilligSkattetrekkData
    messages: Message[] | null
}

export interface FrivilligSkattetrekkData {
    tilleggstrekk: TrekkDTO | null;
    framtidigTilleggstrekk: TrekkDTO | null;
    skattetrekk: ForenkletSkattetrekk | null;
}

export interface UpdateTilleggstrekkRequest {
    value: number;
    satsType: SatsType
}


const isMock = process.env.isMock || false
const PORT = process.env.MOCK_PORT || "8080"
const BASE_URL = isMock ? "http://" + window.location.hostname + ":" + PORT + import.meta.env.BASE_URL + "/"
    : import.meta.env.BASE_URL + "/"

export async function fetchSkattetrekk(): Promise<FrivilligSkattetrekkResponse> {
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

    return await fetch(BASE_URL+ "api/skattetrekk", {
            method: "GET",
            credentials: "include",
            headers: headers
        }
    ).then(
        response => {
            if (response.status >= 200 && response.status < 300) {
                return response.json().then(data => {
                    return data as FrivilligSkattetrekkResponse;
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

export async function saveSkattetrekk(request: UpdateTilleggstrekkRequest): Promise<FrivilligSkattetrekkResponse> {
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

    return await fetch(BASE_URL+ "api/skattetrekk", {
            method: "POST",
            credentials: "include",
            headers: headers,
            body: JSON.stringify(request)
        }
    ).then(
        response => {
            if (response.status >= 200 && response.status < 300) {
                return response.json().then(data => {
                    return data as FrivilligSkattetrekkResponse;
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

