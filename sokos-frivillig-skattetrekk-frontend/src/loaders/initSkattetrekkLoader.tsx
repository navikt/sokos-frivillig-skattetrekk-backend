import {fetchSkattetrekk} from "@/api/skattetrekkBackendClient";


export const initSkattetrekkLoader = async () => {
    return fetchSkattetrekk()
}