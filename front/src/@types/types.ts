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
    open: number
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
    id: number,
    position: string,
    dateOpen: string,
    dateClose: string | undefined,
    broker: ItemType,
    market: MarketType,
    ticker: TickerType,
    currency: ItemType,
    //-----------------------
    estimatedPriceOpen: number,
    estimatedFees: number,
    estimatedBreakEven: number,
    estimatedItems: number,
    riskToCapitalPc: number,
    risk: number,
    levelPrice: number | undefined,
    atr: number | undefined,
    //-----------------------
    openStopLoss: number,
    openTakeProfit: number,
    brokerInterest: number | undefined,
    totalBought: number | undefined
    totalSold: number | undefined
    itemBought: number | undefined,
    itemSold: number | undefined,
    finalStopLoss: number | undefined,
    finalTakeProfit: number | undefined,
    openCommission: number | undefined,
    closeCommission: number | undefined,
    note: string | undefined,
    totalFeesInCurrency: number | undefined
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
    editDialog: (logItem: TradeLog) => void
}

type PositionOpenType = {
    position: 'short' | 'long'
    dateOpen: string,
    brokerId: number,
    marketId: number,
    tickerId: number,
    itemNumber: number,
    atr: number,
    levelPrice: number,
    priceOpen: number,
    stopLoss: number,
    takeProfit: number | undefined,
    outcomeExpected: number | undefined,
    riskPc: number,
    riskRewardPc: number,
    depositPc: number,
    breakEven: number | undefined,
    fees: number,
    totalBought: number | undefined,
    totalSold: number | undefined,
    note: string | undefined
}

type PositionCloseType = {
    id: number,
    quantity: number,
    dateClose: string,
    priceClose: number,
    brokerInterest: number,
    totalBought: number | undefined,
    totalSold: number | undefined,
    fees: number,
    note: string | undefined
}

type PositionEditType = {
    id: number,
    stopLoss: number,
    takeProfit: number | undefined,
    note: string | undefined
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
    currentBroker: ItemType,
    markets: MarketType [],
    tickers: TickerType [],
    isOpen: boolean,
    open: (open: PositionOpenType) => void,
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

export {
    DatePickerBoxProps,
    ValueFieldBoxProps,
    SelectFieldBoxProps,
    NumberFieldBoxProps,
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
    PositionEditType,
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
