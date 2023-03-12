/* eslint-disable @typescript-eslint/no-explicit-any */
import { SelectChangeEvent } from '@mui/material/Select'

type ItemType = { id: number, name: string }

interface TickerType extends ItemType {
    longName: string,
    currency: ItemType
}

interface MarketType extends ItemType {
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
    open: number,
    borrowed?: number
}

type MoneyStateType = {
    capital: number,
    profit: number,
    riskBase: number
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
    logPage: TradeLogPageType | undefined,
    isLoading: boolean,
    currentBroker: ItemType | undefined,
    open: (open: PositionOpenType) => void,
    edit: (edit: PositionEditType) => void,
    close: (close: PositionCloseType) => void,
    page: (page: number) => void,
    setCurrentBrokerId: (id: number) => void,
    refill: (currencyId: number, amount: number) => void,
    correction: (currencyId: number, amount: number) => void,
    exchange: (currencyFromId: number,
               currencyToId: number,
               amountFrom: number,
               amountTo: number) => void
}

type BrokerProps = {
    brokers: ItemType[] | undefined,
    currencies: ItemType[] | undefined,
    currentBroker: ItemType | undefined,
    setCurrentBrokerId: (id: number) => void,
    refill: (currencyId: number, amount: number) => void,
    correction: (currencyId: number, amount: number) => void,
    exchange: (currencyFromId: number,
               currencyToId: number,
               amountFrom: number,
               amountTo: number) => void
}

type ButtonProps = {
    text: string,
    onClick: () => void,
    style?: object
}

type RefillDialogProps = {
    open: boolean,
    title: string,
    negativeAllowed: boolean,
    onSubmit: (currencyId: number, amount: number) => void,
    onCancel: () => void,
    currencies: ItemType[] | undefined
}

type ExchangeDialogProps = {
    open: boolean,
    onExchange: (currencyFromId: number,
                 currencyToId: number,
                 amountFrom: number,
                 amountTo: number) => void,
    onCancel: () => void,
    currencies: ItemType[] | undefined
}

type SelectorProps = {
    label?: string,
    items: ItemType[],
    value: string,
    onChange: (event: SelectChangeEvent<unknown>) => void,
    variant?: string
}

type ExchangeType = {
    brokerId: number,
    currencyFromId: number,
    currencyToId: number,
    amountFrom: number,
    amountTo: number
}

type PageRequest = {
    pageSize: number,
    pageNumber: number
}

type EvalRequest = {
    brokerId: number,
    tickerId: number,
    priceOpen: number,
    items?: number,
    stopLoss?: number,
    date: string,
    short: boolean
}

type TradeLog = {
    id: number,
    position: string,
    dateOpen: string,
    dateClose: string | undefined,
    broker: ItemType,
    market: MarketType,
    ticker: TickerType,
    currency: ItemType,
    itemNumber: number,
    priceOpen: number,
    priceClose: number | undefined,
    volume: number,
    volumeToDeposit: number,
    stopLoss: number,
    takeProfit: number | undefined,
    outcomeExpected: number | undefined,
    risk: number | undefined,
    breakEven: number | undefined,
    fees: number,
    outcome: number | undefined,
    outcomePercent: number | undefined,
    profit: number | undefined,
    note: string | undefined,
    chart: string | undefined,
    grade: string | undefined,
    goal: number | undefined,
    brokerInterest: number | undefined
    parentId: number | undefined
}

type SortType = {
    empty: boolean,
    sorted: boolean,
    unsorted: boolean
}

type PageableType = {
    sort: SortType,
    offset: number,
    pageNumber: number,
    pageSize: number,
    paged: boolean,
    unpaged: boolean
}

type TradeLogPageType = {
    content: TradeLog [],
    pageable: PageableType,
    first: boolean,
    last: boolean,
    totalPages: number
    totalElements: number
    size: number
    number: number
    sort: SortType
    numberOfElements: number,
    empty: boolean
}

interface ExpandButtonProps {
    expanded: boolean,
    onClick: (expanded: boolean) => void
}
interface CloseButtonProps {
    onClick: () => void
}

type PositionOpenType = {
    position: 'short' | 'long'
    dateOpen: string,
    brokerId: number,
    marketId: number,
    tickerId: number,
    itemNumber: number,
    priceOpen: number,
    stopLoss: number,
    takeProfit: number | undefined,
    outcomeExpected: number | undefined,
    risk: number,
    breakEven: number | undefined,
    fees: number,
    note: string | undefined
}

type PositionCloseType = {
    id: number,
    quantity: number,
    dateClose: string,
    priceClose: number,
    brokerInterest: number,
    note: string | undefined
}

type PositionEditType = {
    id: number,
    stopLoss: number,
    takeProfit: number | undefined,
    note: string | undefined
}

export {
    MarketType,
    ItemType,
    TradeContextType,
    BrokerProps,
    BrokerStatsType,
    MoneyStateType,
    RefillType,
    ExchangeType,
    ButtonProps,
    RefillDialogProps,
    ExchangeDialogProps,
    SelectorProps,
    TradeLogPageType,
    TradeLog,
    TickerType,
    PageRequest,
    ExpandButtonProps,
    PositionOpenType,
    EvalRequest,
    PositionCloseType,
    CloseButtonProps,
    PositionEditType
}
