import { numberFormatWithKr } from '@/common/Utils'
import './FormatKroner.css'

export const FormatKroner = ({ value }: { value: number }) => {
  return <span id="format-kroner">{numberFormatWithKr(value)}</span>
}
