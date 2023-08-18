/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { Dispatch } from 'react'

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
    amount: number
}

type BrokerStatsType = {
    tradeAccounts: BrokerAccount[],
    outcome: number,
    open: number,
    riskBase: number
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
    brokers: ItemType[] | undefined
    currencies: ItemType[] | undefined
    tickers: TickerType[]
    markets: MarketType[]
    brokerStats: BrokerStatsType
    moneyState: MoneyStateType
    logPage: TradeLogPageType
    isLoading: boolean
    currentBroker: ItemType | undefined
    open: (open: PositionOpenType) => void
    close: (close: PositionCloseType) => void
    page: (page: number) => void
    setCurrentBrokerId: (id: number) => void
    refill: (currentBrokerId: number, currencyId: number, amount: number) => void
    correction: (currentBrokerId: number, currencyId: number, amount: number) => void
    exchange: (currentBrokerId: number,
               currencyFromId: number,
               currencyToId: number,
               amountFrom: number,
               amountTo: number) => Promise<any>,
    refreshDashboard: () => void
    setOpenDialogVisible: (visible: boolean) => void
    openDialogVisible: boolean,
    refillDialogVisible: boolean,
    exchangeDialogVisible: boolean,
    setRefillDialogVisible: (visible: boolean) => void
    setExchangeDialogVisible: (visible: boolean) => void
    correctionDialogVisible: boolean,
    setCorrectionDialogVisible: (visible: boolean) => void
    currentView: 0 | 1,
    setCurrentView: (view: 0 | 1) => void
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
    onClose: () => void,
    currencies: ItemType[] | undefined
}

type SelectorProps = {
    label?: string,
    items: ItemType[],
    value: string,
    name: string,
    color?: string,
    dispatch: Dispatch<FormAction>,
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
    price: number,
    atr: number,
    items: number,
    stopLoss: number,
    takeProfit: number,
    date: string,
    short: boolean
}

type EvalToFitRequest = {
    brokerId: number,
    tickerId: number,
    levelPrice: number,
    atr: number,
    riskPc: number,
    riskRewardPc: number,
    depositPc: number,
    stopLoss?: number,
    date: string,
    short: boolean,
    technicalStop: boolean
}

type TradeLog = {
    id: number
    position: string
    dateOpen: string
    dateClose: string | undefined
    broker: ItemType
    market: MarketType
    ticker: TickerType
    currency: ItemType
    //-----------------------
    estimatedPriceOpen: number
    estimatedFees: number
    estimatedBreakEven: number
    estimatedItems: number
    riskToCapitalPc: number
    risk: number
    levelPrice: number | undefined
    atr: number | undefined
    //-----------------------
    openStopLoss: number
    openTakeProfit: number
    brokerInterest: number | undefined
    totalBought: number | undefined
    totalSold: number | undefined
    itemBought: number | undefined
    itemSold: number | undefined
    finalStopLoss: number | undefined
    finalTakeProfit: number | undefined
    openCommission: number | undefined
    closeCommission: number | undefined
    note: string | undefined
    outcome: number | undefined
    outcomePc: number | undefined
    partsClosed: number | undefined
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

interface ExpandableProps {
    logItem: TradeLog,
    expandHandler: (expanded: boolean) => void
    closeDialog: (logItem: TradeLog) => void
}

type PositionOpenType = {
    position: 'short' | 'long'
    dateOpen: string,
    brokerId: number,
    marketId: number,
    tickerId: number,

    estimatedPriceOpen: number,
    estimatedFees: number,
    estimatedBreakEven: number,
    estimatedItems: number,
    riskToCapitalPc: number,
    risk: number,
    levelPrice: number | undefined,
    atr: number | undefined,

    openStopLoss: number,
    openTakeProfit: number | undefined,
    itemBought: number | undefined,
    itemSold: number | undefined,
    totalBought: number | undefined,
    totalSold: number | undefined,
    openCommission: number,
    note: string | undefined
}

type PositionCloseType = {
    id: number,
    itemBought: number | undefined,
    itemSold: number | undefined,
    totalBought: number | undefined,
    totalSold: number | undefined,
    dateClose: string,
    note: string | undefined
    brokerInterest: number | undefined,
    closeCommission: number | undefined,
    finalStopLoss: number | undefined,
    finalTakeProfit: number | undefined
}

type FormFieldNumeric = {
    name: string,
    value: number | string | undefined,
    valid: boolean
    errorText?: string
}

type FormFieldString = {
    name: string,
    value: string | undefined,
    valid: boolean
}

type FormFieldDate = {
    name: string,
    value: Date | undefined,
    valid: boolean
}

type FormState = {
    isInitialized: boolean,
    isValid: boolean,
    valuesNumeric: FormFieldNumeric[],
    valuesString: FormFieldString[],
    valuesDate: FormFieldDate[]
}

type FormActionPayload = {
    name?: string,
    valueNum?: number,
    valueStr?: string,
    valueDate?: Date,
    valid?: boolean,
    errorText?: string,
    valuesNumeric?: FormFieldNumeric[],
    valuesString?: FormFieldString[],
    valuesDate?: FormFieldDate[]
}

type FormOptions = {
    formState: FormState,
    dispatch: React.Dispatch<FormAction>
}

type FormAction = {
    type: 'set' | 'reset' | 'init' | 'remove' | 'clearErrors',
    payload: FormActionPayload
}

interface OpenDialogProps {
    isOpen: boolean,
    onClose: () => void
}

interface NumberFieldBoxProps {
    label: string,
    value: number | undefined,
    dispatch: React.Dispatch<FormAction>
    fieldName: string
    zeroAllowed?: boolean,
    color?: string,
    errorText?: string,
    valid?: boolean
}

interface SelectFieldBoxProps {
    label: string,
    value: string,
    items: ItemType[],
    variant?: string,
    fieldName: string,
    color?: string,
    dispatch: React.Dispatch<FormAction>
}

interface ValueFieldBoxProps {
    label: string,
    value: string | number | undefined,
    variant?: string,
    color?: object
}

interface DatePickerBoxProps {
    label: string,
    fieldName: string,
    dispatch: React.Dispatch<FormAction>
}

type StatsType = {
    // Common
    trades: number,
    dayWithTradesDayRatio: number,
    partials: number,
    tradesPerDayMax: number,
    tradesPerDayAvg: number,
    // Volume
    volumePerTradeMax: number,
    volumePerTradeAvg: number,
    volumePerDayMax: number,
    volumePerDayAvg: number,
    volume: number,
    // Commissions
    commissionsPerTradeAvg: number,
    commissions: number,
    // Profit
    profitPerTradeAvg: number,
    profitPerDayAvg: number,
    profitPerDayMax: number,
    profitPartialsAvg: number,
    profitSinglesAvg: number
    profit: number,
    profitVolumePc: number, // Profit/Volume
    profitCapitalPc: number, // Profit/Capital (at the start of the period)
    // Loss
    loss: number,
    lossPerTradeAvg: number,
    lossPerTradeMax: number,
    // Quality
    riskRewardRatioAvg: number,
    riskRewardRatioMax: number,
    winRate: number, // Trades with profit / Total trades
    capitalTurnover: number, // Capital turnover
    slippageAvg: number,
    takeDeltaAvg: number, // TakeProfit estimate - TakeProfit real (for positive trades)
    stopDeltaAvg: number, // StopLoss estimate - StopLoss real (for negative trades)
    // Money
    capital: number,
    refills: number,
    withdrawals: number,
    capitalChange: number // Capital change during the period
}

export {
    StatsType,
    DatePickerBoxProps,
    ValueFieldBoxProps,
    SelectFieldBoxProps,
    NumberFieldBoxProps,
    MarketType,
    ItemType,
    TradeContextType,
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
    ExpandableProps,
    FormState,
    FormFieldNumeric,
    FormFieldString,
    FormFieldDate,
    FormAction,
    FormActionPayload,
    FormOptions,
    OpenDialogProps,
    EvalToFitRequest
}
