export const parseInntekt = (s: string) => {
    if (!s) return 0
    if (s.includes('.')) {
        return NaN
    }
    return Number(s.replace(/\s+/g, ''))
}