import React, { createContext, useEffect, useMemo, useState } from 'react'
import { useQuery } from 'react-query'
import {
    fetchBrokers,
    fetchBrokerStats,
    fetchCurrencies,
    fetchMarkets,
    fetchMoneyState,
    fetchTickers,
    postExchange,
    postLogPage,
    postOpen,
    postClose,
    postRefill, postCorrection, postEdit
} from './api'
import {
    BrokerStatsType,
    ItemType,
    PositionCloseType,
    PositionEditType,
    PositionOpenType,
    TradeContextType,
    TradeLogPageType
} from 'types'

const onetimeQueryOptions = () => {
    return {
        refetchOnMount: false,
        refetchOnWindowFocus: false,
        refetchOnReconnect: false,
        refetchInterval: false as const,
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
    const { data, isLoading } = useQuery(key + brokerId, () => {
        return fetchBrokerStats(brokerId)
    }, onetimeQueryOptions())

    return {
        brokerStats: data as BrokerStatsType,
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

export const PAGE_SIZE = 10

const useLogPage = (key: string, page: number) => {
    const { data, isLoading } = useQuery(key, () => {
        return postLogPage({
            pageSize: PAGE_SIZE,
            pageNumber: page
        })
    }, onetimeQueryOptions())

    return {
        logPage: data as TradeLogPageType,
        isLoadingLogPage: isLoading
    }
}

const useTrade = (): TradeContextType => {
    const { brokers, isLoadingBrokers } = useBrokers()
    const { currencies, isLoadingCurrencies } = useCurrencies()
    const { tickers, isLoadingTickers } = useTickers()
    const { markets, isLoadingMarkets } = useMarkets()
    const [currentBrokerId, setCurrentBrokerId] = useState(0)
    const [brokerStatsKey, setBrokerStatsKey] = useState('brokerKey')
    const [moneyStateKey, setMoneyStateKey] = useState('moneyKey')
    const [pageLogKey, setPageLogKey] = useState('pageLogKey')
    const [pageNum, setPageNum] = useState(0)
    const { brokerStats, isLoadingBrokerStats } = useBrokerStats(currentBrokerId, brokerStatsKey)
    const { moneyState, isLoadingMoneyState } = useMoneyState(moneyStateKey)
    const { logPage, isLoadingLogPage } = useLogPage(pageLogKey, pageNum)
    const [ apiError, setApiError ] = useState<string | undefined>(undefined)

    const currentBroker = (): ItemType | undefined => {
        return isLoadingBrokers ? undefined : brokers.find((elem: ItemType) => {
            return elem.id == currentBrokerId
        })
    }

    useEffect(() => {
        if (brokers && brokers.length > 0 && currentBrokerId == 0) {
            setCurrentBrokerId(brokers[0].id)
        }
    })

    const refill = (currencyId: number, amount: number) => {
        setApiError(undefined)
        postRefill({
            brokerId: currentBrokerId,
            currencyId,
            amount
        }).then(() => {
            setMoneyStateKey('' + Date.now())
            setBrokerStatsKey('' + Date.now())
        })
    }

    const correction = (currencyId: number, amount: number) => {
        setApiError(undefined)
        postCorrection({
            brokerId: currentBrokerId,
            currencyId,
            amount
        }).then(() => {
            setMoneyStateKey('' + Date.now())
            setBrokerStatsKey('' + Date.now())
        })
    }

    const page = (page: number) => {
        setPageNum(page)
        setPageLogKey('' + Date.now())
    }

    const exchange = (currencyFromId: number,
                      currencyToId: number,
                      amountFrom: number,
                      amountTo: number) => {
        setApiError(undefined)
        postExchange({
            brokerId: currentBrokerId,
            currencyFromId,
            currencyToId,
            amountFrom,
            amountTo
        }).then(() => {
            setMoneyStateKey('' + Date.now())
            setBrokerStatsKey('' + Date.now())
        }).catch((err) => {
            setApiError(err.message)
        })
    }


    const open = (open: PositionOpenType) => {
        setApiError(undefined)
        postOpen(open).then(
            () => {
                setMoneyStateKey('' + Date.now())
                setBrokerStatsKey('' + Date.now())
                setPageLogKey('' + Date.now())
            }
        )
    }

    const close = (close: PositionCloseType) => {
        setApiError(undefined)
        postClose(close).then(
            () => {
                setMoneyStateKey('' + Date.now())
                setBrokerStatsKey('' + Date.now())
                setPageLogKey('' + Date.now())
            }
        )
    }

    const edit = (edit: PositionEditType) => {
        setApiError(undefined)
        postEdit(edit).then(
            () => {
                setPageLogKey('' + Date.now())
            }
        )
    }

    return {
        apiError,
        brokers,
        currencies,
        tickers,
        markets,
        brokerStats,
        moneyState,
        logPage,
        open,
        close,
        edit,
        page,
        refill,
        correction,
        exchange,
        isLoading: isLoadingBrokers || isLoadingCurrencies || isLoadingMarkets || isLoadingTickers ||
            isLoadingBrokerStats || isLoadingMoneyState || isLoadingLogPage,
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
