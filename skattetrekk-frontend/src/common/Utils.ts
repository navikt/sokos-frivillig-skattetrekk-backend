
import {format} from 'date-fns'
import {ForenkletSkattetrekk, SatsType, TrekkDTO} from "@/api/skattetrekkBackendClient";

export function numberFormatWithKr(value: number): string {
  return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ' ') + ' kr'
}

export function numberFormat(value: number): string {
  return value.toLocaleString('no-NO')
}

export function formatDateTime(value: Date): string {
  const date = new Date(value)
  return (
    date.toLocaleDateString('no-NO', { day: 'numeric', month: 'long', year: 'numeric' }) +
    ' kl. ' +
    date.toLocaleTimeString('no-NO', { hour: '2-digit', minute: '2-digit' })
  )
}

export function formatDate(value: Date): string {
  return format(value, 'dd.MM.yyyy')
}

export function formatDateLong(value: Date): string {
  const date = new Date(value)
  return date.toLocaleDateString('no-NO', { day: 'numeric', month: 'long', year: 'numeric' })
}

export const parseInntekt = (s: string | null) => {
  if (!s) return 0
  if (s.includes('.')) {
    return NaN
  }
  return Number(s.replace(/\s+/g, ''))
}

export function showPercentageOrTable(skattetrekk: ForenkletSkattetrekk) {
  if (skattetrekk.tabellNr != null) {
    return `Tabell ${skattetrekk.tabellNr}`
  } else if (skattetrekk.prosentsats != null) {
    return `${skattetrekk.prosentsats} %`
  }
}

export function visProsentEllerBelop(tilleggstrekk: TrekkDTO | null) {
  if (tilleggstrekk == null) {
    return "Ingen"
  }

  if (tilleggstrekk.satsType == SatsType.PROSENT && tilleggstrekk.sats != null) {
    return `${tilleggstrekk.sats} %`
  } else if (tilleggstrekk.satsType == SatsType.KRONER && tilleggstrekk.sats != null) {
    return `${numberFormatWithKr(tilleggstrekk.sats)} per måned`
  }

  return "Ingen"
}
