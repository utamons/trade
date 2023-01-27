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

type BrokerAccount = {
    id: number,
    name: string,
    currency: ItemType,
    broker: ItemType,
    type: string,
    amount: number,
    updatedAt: Date
}

type BrokerStatsType = {
    accounts: BrokerAccount[],
    outcome: number,
    avgOutcome: number,
    avgProfit: number,
    open: number
}

type MoneyStateType = {
    capital: number,
    profit: number
}

type RefillType = {
    brokerId: number,
    currencyId: number,
    amount: number
}

type TradeContextType = {
    brokers: ItemType[] | undefined,
    currencies: ItemType[] | undefined,
    tickers: TickerType[] | undefined,
    markets: MarketType[] | undefined,
    brokerStats: BrokerStatsType | undefined,
    moneyState: MoneyStateType | undefined,
    isLoading: boolean,
    currentBroker: ItemType | undefined,
    setCurrentBrokerId: (id: number) => void,
    refill: (currencyId: number, amount: number) => void
}

type BrokerProps = {
    brokers: ItemType[] | undefined,
    currencies: ItemType[] | undefined,
    currentBroker: ItemType | undefined,
    setCurrentBrokerId: (id: number) => void,
    refill: (currencyId: number, amount: number) => void
}

export {
    MarketType,
    ItemType,
    TradeContextType,
    BrokerProps,
    BrokerStatsType,
    MoneyStateType,
    RefillType
}
