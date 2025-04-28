
import {format} from 'date-fns'

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
