// noinspection TypeScriptValidateTypes

import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import Dialog from '@mui/material/Dialog'
import { BLUE, greaterColor, remCalc, roundTo2, takeColor } from '../../utils/utils'
import Button from '../tools/button'
import React, { Dispatch, useCallback, useContext, useEffect, useState } from 'react'
import { ButtonContainerStyled, FieldName, NoteBox, RedSwitch, SwitchBox } from '../../styles/style'
import { Box, Grid } from '@mui/material'
import TextField from '@mui/material/TextField'
import { FormAction, FormActionPayload, FormState, OpenDialogProps, TickerType } from 'types'
import { postEval, postEvalToFit } from '../../api'
import { useTheme } from '@emotion/react'
import CircularProgress from '@mui/material/CircularProgress'
import { getFieldErrorText, getFieldValue, isFieldValid, useForm } from './dialogUtils'
import NumberFieldBox from './components/numberFieldBox'
import SelectFieldBox from './components/selectFieldBox'
import ValueFieldBox from './components/valueFieldBox'
import DatePickerBox from './components/datePickerBox'
import { MAX_DEPOSIT_PC, MAX_RISK_PC, MAX_RISK_REWARD_PC } from '../../utils/constants'
import CheckFieldBox from './components/checkFieldBox'
import { TradeContext } from '../../trade-context'
import Alert from '@mui/material/Alert'


const positions = [
    { id: 0, name: 'long (buy)' },
    { id: 1, name: 'short (sell)' }
]

interface Eval {
    outcomeExp: number,
    gainPc: number,
    fees: number,
    riskPc: number,
    riskRewardPc: number,
    breakEven: number,
    volume: number,
    risk: number
}

interface EvalToFit {
    outcomeExp: number,
    gainPc: number,
    fees: number,
    price: number,
    riskPc: number,
    riskRewardPc: number,
    breakEven: number,
    takeProfit: number,
    stopLoss: number,
    items: number
    volume: number,
    depositPc: number,
    risk: number
}

const resetPayload = (stopPrice: number | undefined, atr: number | undefined, tickerId: string, marketId: string, positionId: string ) => {
    return {
        values: [
            {
                name: 'stopPrice',
                valid: true,
                value: stopPrice
            },
            {
                name: 'atr',
                valid: true,
                value: atr
            },
            {
                name: 'tickerId',
                valid: true,
                value: tickerId
            },
            {
                name: 'marketId',
                valid: true,
                value: marketId
            },
            {
                name: 'positionId',
                valid: true,
                value: positionId
            },
            {
                name: 'riskRewardPc',
                valid: true,
                value: MAX_RISK_REWARD_PC
            },
            {
                name: 'depositPc',
                valid: true,
                value: MAX_DEPOSIT_PC
            },
            {
                name: 'riskPc',
                valid: true,
                value: MAX_RISK_PC
            },
            {
                name: 'date',
                valid: true,
                value: new Date()
            }
        ]
    }
}

const initFormState = (formState: FormState, dispatch: Dispatch<FormAction>, tickerId: number, marketId: number, positionId: number) => {
    if (formState.isInitialized)
        return

    const payload: FormActionPayload = {
        values: [
            {
                name: 'currentPrice',
                valid: true,
                value: undefined
            },
            {
                name: 'openCommission',
                valid: true,
                value: undefined
            },
            {
                name: 'risk',
                valid: true,
                value: undefined
            },
            {
                name: 'totalSold',
                valid: true,
                value: undefined
            },
            {
                name: 'totalBought',
                valid: true,
                value: undefined
            },
            {
                name: 'riskRewardPc',
                valid: true,
                value: MAX_RISK_REWARD_PC
            },
            {
                name: 'depositPc',
                valid: true,
                value: MAX_DEPOSIT_PC
            },
            {
                name: 'tickerId',
                valid: true,
                value: tickerId
            },
            {
                name: 'marketId',
                valid: true,
                value: marketId
            },
            {
                name: 'positionId',
                valid: true,
                value: positionId
            },
            {
                name: 'limit',
                valid: true,
                value: undefined
            },
            {
                name: 'items',
                valid: true,
                value: undefined
            },
            {
                name: 'stopLoss',
                valid: true,
                value: undefined
            },
            {
                name: 'takeProfit',
                valid: true,
                value: undefined
            },
            {
                name: 'outcomeExp',
                valid: true,
                value: undefined
            },
            {
                name: 'riskPc',
                valid: true,
                value: MAX_RISK_PC
            },
            {
                name: 'breakEven',
                valid: true,
                value: undefined
            },
            {
                name: 'gainPc',
                valid: true,
                value: undefined
            },
            {
                name: 'stopPrice',
                valid: true,
                value: undefined
            },
            {
                name: 'atr',
                valid: true,
                value: undefined
            },
            {
                name: 'volume',
                valid: true,
                value: undefined
            },
            {
                name: 'fees',
                valid: true,
                value: undefined
            },
            {
                name: 'note',
                valid: true,
                value: undefined
            },
            {
                name: 'date',
                valid: true,
                value: new Date()
            }
        ]
    }

    dispatch({ type: 'init', payload })
}

const getCorrectedAtr = (atr: number, currentPrice: number, stopPrice: number) => {
    if (currentPrice) {
        return atr - Math.abs(currentPrice - stopPrice)
    }
    return atr
}

export default ({ onClose, isOpen }: OpenDialogProps) => {
    const { currentBroker, markets, tickers, open } = useContext(TradeContext)
    const { formState, dispatch } = useForm()
    const [currentTicker, setCurrentTicker] = useState<TickerType | undefined>(tickers?.[0])
    const [evaluate, setEvaluate] = useState(true)
    const [isLoading, setLoading] = useState(false)
    const [technicalStop, setTechnicalStop] = useState(false)
    const [warning, setWarning] = useState<'set'|'unset'|'disabled'>('unset')
    const [warningText, setWarningText] = useState<string>('')

    if (!currentBroker)
        return null

    const theme = useTheme()
    // noinspection TypeScriptUnresolvedVariable
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    const defaultColor = theme.palette.text.primary

    const { isValid } = formState

    const currentPrice = getFieldValue('currentPrice', formState) as number
    const tickerId = '' + getFieldValue('tickerId', formState)
    const marketId = '' + getFieldValue('marketId', formState)
    const positionId = '' + getFieldValue('positionId', formState)
    const limit = getFieldValue('limit', formState) as number
    const items = getFieldValue('items', formState) as number
    const stopLoss = getFieldValue('stopLoss', formState) as number
    const takeProfit = getFieldValue('takeProfit', formState) as number
    const outcomeExp = getFieldValue('outcomeExp', formState) as number
    const riskRewardPc = getFieldValue('riskRewardPc', formState) as number
    const riskPc = getFieldValue('riskPc', formState) as number
    const depositPc = getFieldValue('depositPc', formState) as number
    const breakEven = getFieldValue('breakEven', formState) as number
    const fees = getFieldValue('fees', formState) as number
    const note = getFieldValue('note', formState) as string
    const date = getFieldValue('date', formState) as Date
    const stopPrice = getFieldValue('stopPrice', formState) as number
    const atr = getFieldValue('atr', formState) as number
    const volume = getFieldValue('volume', formState) as number
    const gainPc = getFieldValue('gainPc', formState) as number
    const totalBought = getFieldValue('totalBought', formState) as number
    const totalSold = getFieldValue('totalSold', formState) as number
    const openCommission = getFieldValue('openCommission', formState) as number
    const risk = getFieldValue('risk', formState) as number

    const isShort = () => positionId == '1'

    const breakEvenPercentageStr = () => {
        if (breakEven && limit)
            return `(${roundTo2(Math.abs(breakEven / (limit / 100) - 100))}%)`
        else
            return ''
    }

    const validateStopLoss = (state: boolean) => {
        if (limit > 0 && stopLoss > 0 && isShort() && limit > stopLoss) {
            dispatch({
                type: 'set',
                payload: { name: 'stopLoss', valid: false, errorText: 'must be greater than limit' }
            })
            state = false
        }
        if (limit > 0 && stopLoss > 0 && !isShort() && limit < stopLoss) {
            dispatch({ type: 'set', payload: { name: 'stopLoss', valid: false, errorText: 'must be less than limit' } })
            state = false
        }
        return state
    }

    const validEvalToFit = () => {
        dispatch({ type: 'clearErrors', payload: {} })
        let state = true
        if (stopPrice == undefined) {
            dispatch({
                type: 'set',
                payload: { name: 'stopPrice', value: undefined, valid: false, errorText: 'required' }
            })
            state = false
        }
        if (stopPrice < 0) {
            dispatch({
                type: 'set',
                payload: { name: 'stopPrice', value: stopPrice, valid: false, errorText: 'must be greater than 0' }
            })
            state = false
        }
        if (atr == undefined) {
            dispatch({
                type: 'set',
                payload: { name: 'atr', value: undefined, valid: false, errorText: 'required' }
            })
            state = false
        }
        if (atr < 0) {
            dispatch({
                type: 'set',
                payload: { name: 'atr', value: atr, valid: false, errorText: 'must be greater than 0' }
            })
            state = false
        }
        if (riskPc == undefined) {
            dispatch({
                type: 'set',
                payload: { name: 'riskPc', value: undefined, valid: false, errorText: 'required' }
            })
            state = false
        }
        if (riskPc < 0) {
            dispatch({
                type: 'set',
                payload: { name: 'riskPc', value: riskPc, valid: false, errorText: 'must be greater than 0' }
            })
            state = false
        }
        if (riskRewardPc == undefined) {
            dispatch({
                type: 'set',
                payload: { name: 'riskRewardPc', value: undefined, valid: false, errorText: 'required' }
            })
            state = false
        }
        if (riskRewardPc < 0) {
            dispatch({
                type: 'set',
                payload: {
                    name: 'riskRewardPc',
                    value: riskRewardPc,
                    valid: false,
                    errorText: 'must be greater than 0'
                }
            })
            state = false
        }
        if (depositPc == undefined) {
            dispatch({
                type: 'set',
                payload: { name: 'depositPc', value: undefined, valid: false, errorText: 'required' }
            })
            state = false
        }
        if (depositPc < 0) {
            dispatch({
                type: 'set',
                payload: { name: 'depositPc', value: depositPc, valid: false, errorText: 'must be greater than 0' }
            })
            state = false
        }
        if (stopLoss != undefined && stopLoss < 0) {
            dispatch({
                type: 'set',
                payload: { name: 'stopLoss', value: stopLoss, valid: false, errorText: 'must be greater than 0' }
            })
            state = false
        }
        state = validateStopLoss(state)
        return state
    }

    const handleTechnicalStop = useCallback((technicalStop: boolean) => {
        setTechnicalStop(technicalStop)
        if (technicalStop) {
            dispatch({ type: 'set', payload: { name: 'stopLoss', value: undefined, valid: true } })
        }
    }, [])

    // noinspection DuplicatedCode
    const handleEvalToFit = useCallback(async () => {
        if (evaluate && validEvalToFit()) {
            const correctedAtr = getCorrectedAtr(atr, currentPrice, stopPrice)
            setLoading(true)
            console.log('handleEvalToFit')
            const ev: EvalToFit = await postEvalToFit({
                brokerId: currentBroker.id,
                tickerId: Number(tickerId),
                levelPrice: stopPrice,
                atr: correctedAtr,
                riskPc,
                riskRewardPc,
                depositPc,
                stopLoss,
                date: date.toISOString(),
                short: positionId == '1',
                technicalStop
            })
            setLoading(false)
            dispatch({ type: 'set', payload: { name: 'outcomeExp', value: ev.outcomeExp, valid: true } })
            dispatch({ type: 'set', payload: { name: 'gainPc', value: ev.gainPc, valid: true } })
            dispatch({ type: 'set', payload: { name: 'fees', value: ev.fees, valid: true } })
            dispatch({ type: 'set', payload: { name: 'limit', value: ev.price, valid: true } })
            dispatch({ type: 'set', payload: { name: 'riskPc', value: ev.riskPc, valid: true } })
            dispatch({ type: 'set', payload: { name: 'riskRewardPc', value: ev.riskRewardPc, valid: true } })
            dispatch({ type: 'set', payload: { name: 'breakEven', value: ev.breakEven, valid: true } })
            dispatch({ type: 'set', payload: { name: 'stopLoss', value: ev.stopLoss, valid: true } })
            dispatch({ type: 'set', payload: { name: 'takeProfit', value: ev.takeProfit, valid: true } })
            dispatch({ type: 'set', payload: { name: 'items', value: ev.items, valid: true } })
            dispatch({ type: 'set', payload: { name: 'volume', value: ev.volume, valid: true } })
            dispatch({ type: 'set', payload: { name: 'depositPc', value: ev.depositPc, valid: true } })
            dispatch({ type: 'set', payload: { name: 'risk', value: ev.risk, valid: true } })

            return
        }
    }, [
        evaluate,
        stopLoss,
        stopPrice,
        atr,
        riskPc,
        riskRewardPc,
        depositPc,
        technicalStop,
        positionId,
        currentPrice
    ])

    const validEval = () => {
        dispatch({ type: 'clearErrors', payload: {} })
        let state = true
        if (limit == undefined) {
            dispatch({ type: 'set', payload: { name: 'limit', valid: false, errorText: 'required' } })
            state = false
        }
        if (limit <= 0) {
            dispatch({ type: 'set', payload: { name: 'limit', valid: false, errorText: 'must be greater than 0' } })
            state = false
        }
        if (items == undefined) {
            dispatch({ type: 'set', payload: { name: 'items', valid: false, errorText: 'required' } })
            state = false
        }
        if (items <= 0) {
            dispatch({ type: 'set', payload: { name: 'items', valid: false, errorText: 'must be greater than 0' } })
            state = false
        }
        if (stopLoss == undefined) {
            dispatch({ type: 'set', payload: { name: 'stopLoss', valid: false, errorText: 'required' } })
            state = false
        }
        if (stopLoss <= 0) {
            dispatch({ type: 'set', payload: { name: 'stopLoss', valid: false, errorText: 'must be greater than 0' } })
            state = false
        }
        if (atr == undefined) {
            dispatch({ type: 'set', payload: { name: 'atr', valid: false, errorText: 'required' } })
            state = false
        }
        if (atr <= 0) {
            dispatch({ type: 'set', payload: { name: 'atr', valid: false, errorText: 'must be greater than 0' } })
            state = false
        }
        if (takeProfit == undefined) {
            dispatch({ type: 'set', payload: { name: 'takeProfit', valid: false, errorText: 'required' } })
            state = false
        }
        if (takeProfit <= 0) {
            dispatch({
                type: 'set',
                payload: { name: 'takeProfit', valid: false, errorText: 'must be greater than 0' }
            })
            state = false
        }
        state = validateStopLoss(state)
        if (limit > 0 && takeProfit > 0 && isShort() && limit < takeProfit) {
            dispatch({
                type: 'set',
                payload: { name: 'takeProfit', valid: false, errorText: 'must be less than limit' }
            })
            state = false
        }
        if (limit > 0 && takeProfit > 0 && !isShort() && limit > takeProfit) {
            dispatch({
                type: 'set',
                payload: { name: 'takeProfit', valid: false, errorText: 'must be greater than limit' }
            })
            state = false
        }
        return state
    }

    const validOpen = () => {
        let state = validEval()
        if (Date.now() - date.getTime() < 5 * 60 * 1000 && warning == 'unset') {
            setWarning('set')
            setWarningText('Date is too close to now')
        }
        if (stopPrice == undefined) {
            dispatch({ type: 'set', payload: { name: 'stopPrice', valid: false, errorText: 'required' } })
            state = false
        }
        if (openCommission == undefined) {
            dispatch({ type: 'set', payload: { name: 'openCommission', valid: false, errorText: 'required' } })
            state = false
        }
        if (!isShort() && totalBought == undefined) {
            dispatch({ type: 'set', payload: { name: 'totalBought', valid: false, errorText: 'required' } })
            state = false
        }
        if (isShort() && totalSold == undefined) {
            dispatch({ type: 'set', payload: { name: 'totalSold', valid: false, errorText: 'required' } })
            state = false
        }
        return state
    }


    const handleOpen = useCallback(async () => {
        if (evaluate && validEval()) {
            const correctedAtr = getCorrectedAtr(atr, currentPrice, stopPrice)
            const ev: Eval = await postEval({
                brokerId: currentBroker.id,
                tickerId: Number(tickerId),
                atr: correctedAtr,
                takeProfit,
                price : limit,
                items,
                stopLoss,
                date: date.toISOString(),
                short: positionId == '1'
            })
            // noinspection DuplicatedCode
            dispatch({ type: 'set', payload: { name: 'outcomeExp', value: ev.outcomeExp, valid: true } })
            dispatch({ type: 'set', payload: { name: 'gainPc', value: ev.gainPc, valid: true } })
            dispatch({ type: 'set', payload: { name: 'fees', value: ev.fees, valid: true } })
            dispatch({ type: 'set', payload: { name: 'riskPc', value: ev.riskPc, valid: true } })
            dispatch({ type: 'set', payload: { name: 'riskRewardPc', value: ev.riskRewardPc, valid: true } })
            dispatch({ type: 'set', payload: { name: 'breakEven', value: ev.breakEven, valid: true } })
            dispatch({ type: 'set', payload: { name: 'volume', value: ev.volume, valid: true } })
            dispatch({ type: 'set', payload: { name: 'risk', value: ev.risk, valid: true } })
            return
        }
        if (!evaluate && validOpen() && riskPc != undefined && fees != undefined && outcomeExp != undefined && breakEven != undefined && depositPc != undefined) {
            open({
                position: positionId == '0' ? 'long' : 'short',
                dateOpen: date.toISOString(),
                brokerId: currentBroker.id,
                marketId: Number(marketId),
                tickerId: Number(tickerId),

                estimatedPriceOpen: limit,
                estimatedFees: fees,
                estimatedBreakEven: breakEven,
                estimatedItems: items,
                riskToCapitalPc: riskPc,
                risk,
                levelPrice: stopPrice,
                atr,

                openStopLoss: stopLoss,
                openTakeProfit: takeProfit,
                itemBought: isShort() ? undefined : items,
                itemSold: isShort() ? items : undefined,
                totalBought,
                totalSold,
                openCommission,
                note
            })
            onClose()
        }
    }, [
        isValid,
        evaluate,
        positionId,
        marketId,
        tickerId,
        limit,
        items,
        atr,
        stopLoss,
        date,
        riskPc,
        fees,
        breakEven,
        note,
        takeProfit,
        totalBought,
        totalSold,
        currentPrice,
        outcomeExp])

    useEffect(() => {
        if (tickerId) {
            const ticker = tickers.find((t) => t.id == Number(tickerId))
            setCurrentTicker(ticker)
        }
    }, [tickerId])

    useEffect(() => {
        initFormState(formState, dispatch, tickers[0].id, markets[0].id, positions[0].id)
    }, [formState])

    const handleSwitch = useCallback((_event: React.SyntheticEvent, checked: boolean) => {
        setEvaluate(!checked)
    }, [])

    const handleCancel = useCallback(() => {
        dispatch({ type: 'reset', payload: resetPayload(undefined, undefined, ''+tickers[0].id, ''+markets[0].id, ''+positions[0].id) })
        onClose()
    }, [])

    const handleReset = useCallback(() => {
        dispatch({ type: 'clearErrors', payload: {} })
        dispatch({ type: 'reset', payload: resetPayload(stopPrice, atr, tickerId, marketId, positionId) })
        setTechnicalStop(false)
        setWarning('unset')
        setWarningText('')
    }, [stopPrice, atr, tickerId, marketId, positionId])

    console.log('openDialog render ', getFieldErrorText('riskPc', formState))

    return <Dialog
        maxWidth={false}
        open={isOpen}
    >
        <DialogContent sx={{ fontSize: remCalc(14), fontFamily: 'sans-serif' }}>
            <Grid sx={{ width: remCalc(700) }} container columns={2}>
                <Grid item xs={1}>
                    <SwitchBox>
                        <FieldName>Open:</FieldName>
                        <RedSwitch
                            size="small"
                            checked={!evaluate}
                            onChange={handleSwitch}
                        />
                    </SwitchBox>
                </Grid>
                <Grid item xs={1}>
                    {warning == 'set' ? <Box sx={{ height: '15px', cursor: 'pointer' }}>
                        <Alert onClick={()=>setWarning('disabled')} severity={'warning'}>{warningText}</Alert>
                    </Box> : <></>}
                </Grid>
                <Grid item xs={1}>
                    <Grid container columns={1}>
                        <DatePickerBox
                            label="Date:"
                            fieldName={'date'}
                            dispatch={dispatch}/>
                        <SelectFieldBox
                            color={BLUE}
                            items={positions}
                            value={positionId}
                            fieldName="positionId"
                            dispatch={dispatch}
                            label="Position:"
                            variant="medium"/>
                        <ValueFieldBox
                            label={'Broker:'}
                            value={currentBroker.name}/>
                        <SelectFieldBox
                            items={markets}
                            value={marketId}
                            fieldName="marketId"
                            dispatch={dispatch}
                            label="Market:"
                            variant="small"/>
                        <SelectFieldBox
                            items={tickers}
                            value={tickerId}
                            fieldName="tickerId"
                            dispatch={dispatch}
                            label="Ticker:"
                            variant="small"/>
                        <NumberFieldBox
                            label={'Items:'}
                            value={items}
                            color={BLUE}
                            valid={isFieldValid('items', formState)}
                            errorText={getFieldErrorText('items', formState)}
                            fieldName={'items'}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            label={'Stop Loss:'}
                            value={stopLoss}
                            color={BLUE}
                            valid={isFieldValid('stopLoss', formState)}
                            errorText={getFieldErrorText('stopLoss', formState)}
                            fieldName={'stopLoss'}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            color={takeColor(takeProfit, limit, atr)}
                            label={'Take Profit:'}
                            value={takeProfit}
                            valid={isFieldValid('takeProfit', formState)}
                            errorText={getFieldErrorText('takeProfit', formState)}
                            fieldName={'takeProfit'}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            label={`Limit: (${currentTicker ? (currentTicker.currency.name) : '???'})`}
                            value={limit}
                            fieldName={'limit'}
                            valid={isFieldValid('limit', formState)}
                            errorText={getFieldErrorText('limit', formState)}
                            color={BLUE}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            label={'Stop:'}
                            value={stopPrice}
                            valid={isFieldValid('stopPrice', formState)}
                            errorText={getFieldErrorText('stopPrice', formState)}
                            fieldName={'stopPrice'}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            label={'ATR:'}
                            value={atr}
                            valid={isFieldValid('atr', formState)}
                            errorText={getFieldErrorText('atr', formState)}
                            fieldName={'atr'}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            label={'Curr. price:'}
                            value={currentPrice}
                            valid={isFieldValid('currentPrice', formState)}
                            errorText={getFieldErrorText('currentPrice', formState)}
                            color={greaterColor(depositPc, defaultColor, MAX_DEPOSIT_PC)}
                            fieldName={'currentPrice'}
                            dispatch={dispatch}/>
                    </Grid>
                </Grid>
                <Grid item xs={1}>
                    <Grid container columns={1}>
                        <CheckFieldBox checked={technicalStop} label="Techincal Stop:" onChange={handleTechnicalStop}/>
                        <NumberFieldBox
                            label={'Open comm.:'}
                            value={openCommission}
                            valid={isFieldValid('openCommission', formState)}
                            errorText={getFieldErrorText('openCommission', formState)}
                            fieldName={'openCommission'}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            label={'Risk(%):'}
                            fieldName={'riskPc'}
                            valid={isFieldValid('riskPc', formState)}
                            errorText={getFieldErrorText('riskPc', formState)}
                            color={greaterColor(riskPc, defaultColor, MAX_RISK_PC)}
                            value={riskPc}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            label={'R/R (%):'}
                            fieldName={'riskRewardPc'}
                            valid={isFieldValid('riskRewardPc', formState)}
                            errorText={getFieldErrorText('riskRewardPc', formState)}
                            color={greaterColor(riskRewardPc, defaultColor, MAX_RISK_REWARD_PC)}
                            value={riskRewardPc}
                            dispatch={dispatch}
                        />
                        <NumberFieldBox
                            label={'Vol./depo. (%):'}
                            value={depositPc}
                            valid={isFieldValid('depositPc', formState)}
                            errorText={getFieldErrorText('depositPc', formState)}
                            color={greaterColor(depositPc, defaultColor, MAX_DEPOSIT_PC)}
                            fieldName={'depositPc'}
                            dispatch={dispatch}/>
                        {isShort() ? <NumberFieldBox
                            label={'Total (SLD):'}
                            fieldName={'totalSold'}
                            valid={isFieldValid('totalSold', formState)}
                            errorText={getFieldErrorText('totalSold', formState)}
                            value={totalSold}
                            dispatch={dispatch}
                        /> : <NumberFieldBox
                            label={'Total (BOT):'}
                            fieldName={'totalBought'}
                            valid={isFieldValid('totalBought', formState)}
                            errorText={getFieldErrorText('totalBought', formState)}
                            value={totalBought}
                            dispatch={dispatch}
                        /> }
                        <ValueFieldBox
                            label={'Out. Exp.:'}
                            value={outcomeExp}/>
                        <ValueFieldBox
                            label={'Gain:'}
                            variant={'pc'}
                            value={gainPc}/>
                        <ValueFieldBox
                            label={'Risk:'}
                            value={risk}/>
                        <ValueFieldBox
                            label={'Fees:'}
                            value={fees}/>
                        <ValueFieldBox
                            label={'Break even:'}
                            value={`${breakEven ?? ''} ${breakEvenPercentageStr()}`}/>
                        <ValueFieldBox
                            label={'Volume:'}
                            value={volume}/>
                    </Grid>
                </Grid>
                <Grid item xs={2}>
                    <NoteBox>
                        <TextField
                            id="outlined-textarea"
                            label="Note"
                            multiline
                            style={{ width: '100%', fontSize: remCalc(14) }}
                            onChange={(event) => dispatch({
                                type: 'set',
                                payload: { name: 'note', value: event.target.value, valid: true }
                            })}
                        />
                    </NoteBox>
                </Grid>
            </Grid>
        </DialogContent>

        <DialogActions sx={{ justifyContent: 'center' }}>

            <ButtonContainerStyled>
                {isLoading ? < CircularProgress size={20}/> :
                    <>{evaluate ? <Button style={{ minWidth: remCalc(101) }} text="Evaluate To Fit"
                                          onClick={handleEvalToFit}/> : <></>}
                        <Button style={{ minWidth: remCalc(101) }} text={evaluate ? 'Evaluate' : 'Open'}
                                onClick={handleOpen}/>
                        <Button style={{ minWidth: remCalc(101) }} text="Reset" onClick={handleReset}/>
                        <Button style={{ minWidth: remCalc(101) }} text="Cancel" onClick={handleCancel}/> </>}
            </ButtonContainerStyled>
        </DialogActions>
    </Dialog>
}
