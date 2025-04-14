import {hentSkattetrekk} from "@/api/skattetrekkBackendClient";


export const initSkattetrekkLoader = async () => {
    console.log("initSkattetrekkLoader")
    return hentSkattetrekk()
}