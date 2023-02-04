import React from 'react'
import CircularProgress from '@mui/material/CircularProgress'

export const Loadable = ({ children, isLoading }: { children?: JSX.Element, isLoading?: boolean }): JSX.Element => (
    isLoading ? < CircularProgress size={20}/> : <>{children}</>
)

const leadingZero = (m: number) => {
    return m <= 9 ? '0' + m : m
}

const formatDate = (date: Date) => {
    const d = date.getDate()
    const m = date.getMonth() + 1
    const y = date.getFullYear()
    const h = date.getHours()
    const mm = date.getMinutes()
    return '' + y + '-' + leadingZero(m) + '-' + leadingZero(d) + ' ' + leadingZero(h) + ':' + leadingZero(mm);
}

export const utc2market = (utcStr: string, offset: number) => {
    if (!utcStr)
        return '-'
    const utc = new Date(utcStr).getTime()
    const newDate = new Date(utc + (3600000 * offset))
    return formatDate(newDate)
}

export const currencySymbol = (currencyName: string): string => {
    switch (currencyName) {
        case 'KZT':
            return '₸'
        case 'USD':
            return '$'
        case 'GBP':
            return '£'
        case 'HKD':
            return 'HK$'
        default:
            return '?'
    }
}

export const money = (currencyName: string, amount: number | undefined): string => {
    if (amount == undefined)
        return '-'
    const str = amount.toLocaleString('en-US', {
        maximumFractionDigits: 2,
        minimumFractionDigits: 2
    })
    return `${currencySymbol(currencyName)} ${str}`
}

export const remCalc = (px: number): string => `${(px / 16).toFixed(3)}rem`

export const RED ='#cc3300'
export const GREEN = '#00cc00'

export const riskColor = (risk:number | undefined, defaultColor: string) => {
    if (risk && risk > 2)
        return { color: RED, fontWeight: 'bolder' }
    return { color: defaultColor }
}

export const profitColor = (profit:number | undefined, defaultColor: string) => {
    if (profit && profit < 0)
        return { color: RED, fontWeight: 'bolder' }
    if (profit && profit > 0)
        return { color: GREEN, fontWeight: 'bolder' }

    return { color: defaultColor }
}
