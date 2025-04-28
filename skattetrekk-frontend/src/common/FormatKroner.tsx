import { numberFormatWithKr } from '@/common/Utils'

export const FormatKroner = ({ value }: { value: number }) => {
  return <span style={{ whiteSpace: 'nowrap' }}>{numberFormatWithKr(value)}</span>
}
