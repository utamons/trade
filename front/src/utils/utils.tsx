import React from 'react'
import CircularProgress from '@mui/material/CircularProgress'

export const Loadable = ({ children, isLoading }: { children?: JSX.Element, isLoading?: boolean }): JSX.Element => (
    isLoading ? < CircularProgress size={20}/> : <>{children}</>
)

export const dateTimeWithOffset = (dateStr: string, offset: number) => {
    const date = new Date(dateStr)

    const utc = date.getTime() + (date.getTimezoneOffset() * 60000)
    const newDate = new Date(utc + (3600000 * offset))
    return newDate.toISOString().substring(0, 16).replace('T', ' ')
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

export const money = (currencyName: string, amount: number| undefined): string => {
    if (amount == undefined)
        return '-'
    const str = amount.toLocaleString('en-US', {
        maximumFractionDigits: 2,
        minimumFractionDigits: 2
    })
    return `${currencySymbol(currencyName)} ${str}`
}

export const remCalc = (px: number): string => `${(px / 16).toFixed(3)}rem`
