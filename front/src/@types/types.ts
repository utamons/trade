/* eslint-disable @typescript-eslint/no-explicit-any */
type ItemType = { id: number, name: string }

type TickerType = {
    id: number,
    name: string,
    currency: ItemType
}

type MarketType = {
    id: number,
    name: string,
    timezone: number
}

type TradeContextType = {
    brokers: ItemType[] | undefined,
    currencies: ItemType[] | undefined,
    tickers: TickerType[] | undefined,
    markets: MarketType[] | undefined,
    isLoading: boolean
}

export {
    MarketType,
    ItemType,
    TradeContextType
}
