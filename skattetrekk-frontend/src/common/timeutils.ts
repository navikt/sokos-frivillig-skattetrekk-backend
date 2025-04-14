
export function isoFormatIgnoreTimezone(date: Date){
    const pad = (n: number) => `${Math.floor(Math.abs(n))}`.padStart(2, '0');
    return date.getFullYear() +
        '-' + pad(date.getMonth() + 1) +
        '-' + pad(date.getDate())
}

export function formatterNavStandardDato(dato: string) {
    return dato.split("-").reverse().join(".")
}