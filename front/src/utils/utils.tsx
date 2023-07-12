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
    return '' + y + '-' + leadingZero(m) + '-' + leadingZero(d) + ' ' + leadingZero(h) + ':' + leadingZero(mm)
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
export const ORANGE = '#ff9900'
export const BLUE = '#0099ff'

export const takeColor = (take:number | undefined, price:number | undefined, atr: number | undefined) => {
    if (take && atr && price) {
        const range = Math.abs(take - price)
        if (range > atr)
            return RED
        if (range / atr > 0.7)
            return ORANGE
    }
    return BLUE
}

export const profitColor = (profit:number | undefined, defaultColor: string) => {
    if (profit && profit < 0)
        return { color: RED, fontWeight: 'bolder' }
    if (profit && profit > 0)
        return { color: GREEN, fontWeight: 'bolder' }

    return { color: defaultColor }
}

export const greaterColor = (value:number | undefined, defaultColor: string, maxValue: number) => {
    if (value && value > maxValue)
        return RED
    return defaultColor
}

export const roundTo2 = (num: number | undefined) => {
    if (num == undefined)
        return 0
    return Math.round(num * 100) / 100
}
