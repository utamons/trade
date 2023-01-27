import React, { createContext, useEffect, useMemo, useState } from 'react'
import { useQuery } from 'react-query'
import { fetchBrokers, fetchBrokerStats, fetchCurrencies, fetchMarkets, fetchMoneyState, fetchTickers, postRefill } from './api'
import { ItemType, TradeContextType } from 'types'

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

const useBrokerStats = (brokerId: number, key: string) => {
    const { data, isLoading } = useQuery(key+brokerId, () => {
        return fetchBrokerStats(brokerId)
    }, onetimeQueryOptions())

    return {
        brokerStats: data,
        isLoadingBrokerStats: isLoading
    }
}

const useMoneyState = (key: string) => {
    const { data, isLoading } = useQuery(key, () => {
        return fetchMoneyState()
    }, onetimeQueryOptions())

    return {
        moneyState: data,
        isLoadingMoneyState: isLoading
    }
}

const useTrade = ():TradeContextType => {
    const { brokers, isLoadingBrokers } = useBrokers();
    const { currencies, isLoadingCurrencies } = useCurrencies();
    const { tickers, isLoadingTickers } = useTickers();
    const { markets, isLoadingMarkets } = useMarkets();
    const [ currentBrokerId, setCurrentBrokerId ] = useState(0)
    const [ brokerStatsKey, setBrokerStatsKey ] = useState('key')
    const [ moneyStateKey, setMoneyStateKey ] = useState('key')
    const { brokerStats, isLoadingBrokerStats } = useBrokerStats(currentBrokerId, brokerStatsKey)
    const { moneyState, isLoadingMoneyState } = useMoneyState(moneyStateKey)

    const currentBroker = (): ItemType | undefined => {
        return isLoadingBrokers ? undefined : brokers.find((elem) => {
            return elem.id == currentBrokerId
        })
    }

    console.log('currentBrokerId',currentBrokerId)

    useEffect(() => {
        if (brokers && brokers.length > 0 && currentBrokerId == 0) {
            setCurrentBrokerId(brokers[0].id)
        }
    })

    const refill = (currencyId: number, amount: number) => {
        postRefill({
            brokerId: currentBrokerId,
            currencyId,
            amount
        }).then(() => {
            setMoneyStateKey(''+Date.now())
            setBrokerStatsKey(''+Date.now())
        })
    }

    return {
        brokers,
        currencies,
        tickers,
        markets,
        brokerStats,
        moneyState,
        refill,
        isLoading: isLoadingBrokers || isLoadingCurrencies || isLoadingMarkets || isLoadingTickers ||
            isLoadingBrokerStats || isLoadingMoneyState,
        currentBroker: currentBroker(),
        setCurrentBrokerId
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
