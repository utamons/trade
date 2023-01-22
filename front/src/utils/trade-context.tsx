import React, { createContext, useEffect, useMemo, useState } from 'react'
import { useQuery } from 'react-query'
import { fetchBrokers, fetchCurrencies, fetchMarkets, fetchTickers } from '../api'
import { TradeContextType } from 'types'

const onetimeQueryOptions = () => {
    return {
        refetchOnMount: false,
        refetchOnWindowFocus: false,
        refetchOnReconnect: false,
        refetchInterval: false as false,
        retry: 0
    }
}

const useBrokers = () => {
    const { data, isLoading } = useQuery('fetchBrokers', fetchBrokers, onetimeQueryOptions())

    return {
        brokers: data,
        isLoadingBrokers: isLoading
    }

}

const useCurrencies = () => {
    const { data, isLoading } = useQuery('fetchCurrencies', fetchCurrencies, onetimeQueryOptions())

    return {
        currencies: data,
        isLoadingCurrencies: isLoading
    }
}

const useMarkets = () => {
    const { data, isLoading } = useQuery('fetchMarkets', fetchMarkets, onetimeQueryOptions())

    return {
        markets: data,
        isLoadingMarkets: isLoading
    }
}

const useTickers = () => {
    const { data, isLoading } = useQuery('fetchTickers', fetchTickers, onetimeQueryOptions())

    return {
        tickers: data,
        isLoadingTickers: isLoading
    }
}

const useTrade = ():TradeContextType => {
    const { brokers, isLoadingBrokers } = useBrokers();
    const { currencies, isLoadingCurrencies } = useCurrencies();
    const { tickers, isLoadingTickers } = useTickers();
    const { markets, isLoadingMarkets } = useMarkets();

    return {
        brokers,
        currencies,
        tickers,
        markets,
        isLoading: isLoadingBrokers || isLoadingCurrencies || isLoadingMarkets || isLoadingTickers
    }
}

export const TradeContext = createContext({
    all: null as TradeContextType | null
})

export const TradeProvider = ({ children }: { children: JSX.Element }) => {
    const all = useTrade()
    const value = useMemo(() => ({ all }), [all])

    return (
        <TradeContext.Provider value={value}>
            {children}
        </TradeContext.Provider>
    )
}
