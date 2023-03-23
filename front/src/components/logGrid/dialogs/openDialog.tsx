// noinspection TypeScriptValidateTypes

import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import Dialog from '@mui/material/Dialog'
import { breakEvenColor, RED, remCalc, riskColor, roundTo2, takeColor } from '../../../utils/utils'
import Button from '../../tools/button'
import React, { Dispatch, useCallback, useEffect, useState } from 'react'
import { ButtonContainerStyled, FieldName } from '../../../styles/style'
import { Box, Grid, styled } from '@mui/material'
import TextField from '@mui/material/TextField'
import { FormAction, FormActionPayload, FormState, ItemType, MarketType, PositionOpenType, TickerType } from 'types'
import Select from '../../tools/select'
import NumberInput from '../../tools/numberInput'
import { postEval, postEvalToFit } from '../../../api'
import Switch from '@mui/material/Switch'
import { useTheme } from '@emotion/react'
import CircularProgress from '@mui/material/CircularProgress'
import { BasicDateTimePicker } from '../../tools/dateTimePicker'
import { getFieldValue, useForm } from '../../dialogs/dialogUtils'

interface OpenDialogProps {
    currentBroker: ItemType,
    markets: MarketType [],
    tickers: TickerType [],
    isOpen: boolean,
    open: (open: PositionOpenType) => void,
    onClose: () => void
}

const positions = [
    { id: 0, name: 'long' },
    { id: 1, name: 'short' }
]

export const FieldValue = styled(Box)(() => ({
    display: 'flex',
    alignContent: 'flex-start',
    alignItems: 'center',
    width: remCalc(200)
}))

const FieldBox = styled(Box)(() => ({
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    width: remCalc(300),
    height: remCalc(50)
}))

const SwitchBox = styled(Box)(() => ({
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    width: remCalc(150),
    height: remCalc(50)
}))

const NoteBox = styled(Box)(() => ({
    width: '100%',
    marginTop: remCalc(10)
}))

const RedSwitch = styled(Switch)(() => ({
    '& .MuiSwitch-switchBase.Mui-checked': {
        color: RED,
        '&:hover': {
            backgroundColor: 'rgba(204, 51, 0, 0.2)'
        }
    },
    '& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track': {
        backgroundColor: RED
    }
}))


export const TextFieldStyled = styled(TextField)(() => ({
    '.MuiOutlinedInput-root': {
        borderRadius: remCalc(2),
        fontSize: remCalc(14),
        maxHeight: remCalc(38),
        maxWidth: remCalc(170)
    },
    '.MuiSvgIcon-root ': {
        fontSize: remCalc(18)
    }
}))

interface Eval {
    fees: number,
    risk: number,
    breakEven: number,
    outcomeExp: number,
    takeProfit: number
}

interface EvalToFit {
    price: number,
    fees: number,
    risk: number,
    breakEven: number,
    outcomeExp: number,
    takeProfit: number,
    stopLoss: number,
    items: number
}

const initFormState = (formState: FormState, dispatch: Dispatch<FormAction>, tickerId: number, marketId: number, positionId: number) => {
    if (formState.isInitialized)
        return

    const payload: FormActionPayload = {
        valuesNumeric: [
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
                name: 'price',
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
                name: 'risk',
                valid: true,
                value: undefined
            },
            {
                name: 'breakEven',
                valid: true,
                value: undefined
            },
            {
                name: 'levelPrice',
                valid: true,
                value: undefined
            },
            {
                name: 'atr',
                valid: true,
                value: undefined
            },
            {
                name: 'fees',
                valid: true,
                value: undefined
            }],
        valuesString: [
            {
                name: 'note',
                valid: true,
                value: undefined
            }],
        valuesDate: [
            {
                name: 'date',
                valid: true,
                value: new Date()
            }
        ]
    }

    dispatch({ type: 'init', payload })
}

export default ({ onClose, isOpen, currentBroker, markets, tickers, open }: OpenDialogProps) => {
    const { formState, dispatch } = useForm()
    const [currentTicker, setCurrentTicker] = useState<TickerType | undefined>(tickers[0])
    const [evaluate, setEvaluate] = useState(true)
    const [isLoading, setLoading] = useState(false)

    const theme = useTheme()
    // noinspection TypeScriptUnresolvedVariable
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    const defaultColor = theme.palette.text.primary

    const { isValid } = formState

    const tickerId = '' + getFieldValue('tickerId', formState)
    const marketId = '' + getFieldValue('marketId', formState)
    const positionId = '' + getFieldValue('positionId', formState)
    const price = getFieldValue('price', formState) as number
    const items = getFieldValue('items', formState) as number
    const stopLoss = getFieldValue('stopLoss', formState) as number
    const takeProfit = getFieldValue('takeProfit', formState) as number
    const outcomeExp = getFieldValue('outcomeExp', formState) as number
    const risk = getFieldValue('risk', formState) as number
    const breakEven = getFieldValue('breakEven', formState) as number
    const fees = getFieldValue('fees', formState) as number
    const note = getFieldValue('note', formState) as string
    const date = getFieldValue('date', formState) as Date
    const levelPrice = getFieldValue('levelPrice', formState) as number
    const atr = getFieldValue('atr', formState) as number

    const gain = takeProfit && breakEven && price ? roundTo2(Math.abs(takeProfit - breakEven)/(price/100)) : undefined

    const breakEvenPercentage = () => {
        if (breakEven && price)
            return Math.abs(breakEven / (price / 100) - 100.0)
        else
            return 0
    }

    const breakEvenPercentageStr = () => {
        if (breakEven && price)
            return `(${roundTo2(Math.abs(breakEven / (price / 100) - 100))}%)`
        else
            return ''
    }
    // noinspection DuplicatedCode
    const handleEvalToFit = useCallback(async () => {
        if (evaluate && ((levelPrice && atr) || price)) {
            setLoading(true)
            const ev: EvalToFit = await postEvalToFit({
                brokerId: currentBroker.id,
                tickerId: Number(tickerId),
                price,
                levelPrice,
                atr,
                items,
                stopLoss,
                date: date.toISOString(),
                short: positionId == '1'
            })
            setLoading(false)
            dispatch({ type: 'set', payload: { name: 'price', valueNum: ev.price, valid: true } })
            dispatch({ type: 'set', payload: { name: 'items', valueNum: ev.items, valid: true } })
            dispatch({ type: 'set', payload: { name: 'stopLoss', valueNum: ev.stopLoss, valid: true } })
            // noinspection DuplicatedCode
            dispatch({ type: 'set', payload: { name: 'takeProfit', valueNum: ev.takeProfit, valid: true } })
            dispatch({ type: 'set', payload: { name: 'outcomeExp', valueNum: ev.outcomeExp, valid: true } })
            dispatch({ type: 'set', payload: { name: 'risk', valueNum: ev.risk, valid: true } })
            dispatch({ type: 'set', payload: { name: 'breakEven', valueNum: ev.breakEven, valid: true } })
            dispatch({ type: 'set', payload: { name: 'fees', valueNum: ev.fees, valid: true } })
            return
        }
    }, [
        evaluate,
        price,
        items,
        stopLoss,
        levelPrice,
        atr
    ])

    const handleOpen = useCallback(async () => {
        console.log('isValid', isValid)
        if (price == undefined || items == undefined || stopLoss == undefined || !isValid) {
            console.error('validation failed')
            return
        }
        if (evaluate && price && items && stopLoss) {
            console.log('evaluation')
            const ev: Eval = await postEval({
                brokerId: currentBroker.id,
                tickerId: Number(tickerId),
                atr,
                levelPrice,
                price,
                items,
                stopLoss,
                date: date.toISOString(),
                short: positionId == '1'
            })
            // noinspection DuplicatedCode
            dispatch({ type: 'set', payload: { name: 'takeProfit', valueNum: ev.takeProfit, valid: true } })
            dispatch({ type: 'set', payload: { name: 'outcomeExp', valueNum: ev.outcomeExp, valid: true } })
            dispatch({ type: 'set', payload: { name: 'risk', valueNum: ev.risk, valid: true } })
            dispatch({ type: 'set', payload: { name: 'breakEven', valueNum: ev.breakEven, valid: true } })
            dispatch({ type: 'set', payload: { name: 'fees', valueNum: ev.fees, valid: true } })
            return
        }
        if (price && items && stopLoss && risk && fees) {
            open({
                position: positionId == '0' ? 'long' : 'short',
                dateOpen: date.toISOString(),
                brokerId: currentBroker.id,
                marketId: Number(marketId),
                tickerId: Number(tickerId),
                atr,
                levelPrice,
                itemNumber: items,
                priceOpen: price,
                stopLoss,
                takeProfit,
                outcomeExpected: outcomeExp,
                risk,
                breakEven,
                fees,
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
        price,
        items,
        stopLoss,
        date,
        risk,
        fees,
        breakEven,
        note,
        takeProfit,
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

    const handleSwitch = useCallback((event: React.SyntheticEvent, checked: boolean) => {
        setEvaluate(!checked)
    }, [])

    const handleCancel = useCallback(() => {
        dispatch({ type: 'reset', payload: {} })
        onClose()
    }, [])

    const handleReset = useCallback(() => {
        dispatch({ type: 'reset', payload: {} })
    }, [])

    return <Dialog
        maxWidth={false}
        open={isOpen}
    >
        <DialogContent sx={{ fontSize: remCalc(12), fontFamily: 'sans' }}>
            <Grid sx={{ width: remCalc(700) }} container columns={2}>
                <Grid item xs={2}>
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
                    <Grid container columns={1}>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Date:</FieldName>
                                <FieldValue>
                                    <BasicDateTimePicker name="date" dispatch={dispatch}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Position:</FieldName>
                                <FieldValue>
                                    <Select
                                        items={positions}
                                        value={positionId}
                                        name="positionId"
                                        dispatch={dispatch}
                                        variant="medium"/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Broker:</FieldName>
                                <FieldValue>{currentBroker.name}</FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Market:</FieldName>
                                <FieldValue>
                                    <Select
                                        items={markets}
                                        value={marketId}
                                        name={'marketId'}
                                        dispatch={dispatch}
                                        variant="small"
                                    />
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Ticker:</FieldName>
                                <FieldValue>
                                    <Select
                                        items={tickers}
                                        value={tickerId}
                                        name={'tickerId'}
                                        dispatch={dispatch}
                                        variant="small"
                                    />
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>
                                    Price: {`(${currentTicker ? (currentTicker.currency.name) : '???'})`}
                                </FieldName>
                                <FieldValue>
                                    <NumberInput
                                        value={price}
                                        name={'price'}
                                        dispatch={dispatch}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>
                                    Level Price:
                                </FieldName>
                                <FieldValue>
                                    <NumberInput
                                        value={levelPrice}
                                        name={'levelPrice'}
                                        dispatch={dispatch}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>
                                    ATR:
                                </FieldName>
                                <FieldValue>
                                    <NumberInput
                                        value={atr}
                                        name={'atr'}
                                        dispatch={dispatch}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                    </Grid>
                </Grid>
                <Grid item xs={1}>
                    <Grid container columns={1}>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Items:</FieldName>
                                <FieldValue>
                                    <NumberInput
                                        value={items}
                                        name={'items'}
                                        dispatch={dispatch}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Stop Loss:</FieldName>
                                <FieldValue>
                                    <NumberInput
                                        name={'stopLoss'}
                                        value={stopLoss}
                                        dispatch={dispatch}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Take Profit:</FieldName>
                                <FieldValue>
                                    <NumberInput
                                        color={takeColor(takeProfit, price, atr, defaultColor)}
                                        value={takeProfit}
                                        name={'takeProfit'}
                                        dispatch={dispatch}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Out. Exp.:</FieldName>
                                <FieldValue>
                                    <NumberInput
                                        value={outcomeExp}
                                        name={'outcomeExp'}
                                        dispatch={dispatch}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Gain:</FieldName>
                                <FieldValue>
                                    {gain} %
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Fees:</FieldName>
                                <FieldValue>
                                    {fees}
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Risk:</FieldName>
                                <FieldValue sx={riskColor(risk, defaultColor)}>
                                    {risk} %
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Break even:</FieldName>
                                <FieldValue sx={breakEvenColor(breakEvenPercentage(), defaultColor)}>
                                    {breakEven} {breakEvenPercentageStr()}
                                </FieldValue>
                            </FieldBox>
                        </Grid>
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
                                payload: { name: 'note', valueStr: event.target.value, valid: true }
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
