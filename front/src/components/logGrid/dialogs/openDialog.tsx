// noinspection TypeScriptValidateTypes

import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import Dialog from '@mui/material/Dialog'
import { BLUE, greaterColor, remCalc, roundTo2, takeColor } from '../../../utils/utils'
import Button from '../../tools/button'
import React, { Dispatch, useCallback, useEffect, useState } from 'react'
import { ButtonContainerStyled, FieldName, NoteBox, RedSwitch, SwitchBox } from '../../../styles/style'
import { Grid } from '@mui/material'
import TextField from '@mui/material/TextField'
import { FormAction, FormActionPayload, FormState, OpenDialogProps, TickerType } from 'types'
import { postEval, postEvalToFit } from '../../../api'
import { useTheme } from '@emotion/react'
import CircularProgress from '@mui/material/CircularProgress'
import { getFieldValue, useForm } from '../../dialogs/dialogUtils'
import NumberFieldBox from '../../dialogs/numberFieldBox'
import SelectFieldBox from '../../dialogs/selectFieldBox'
import ValueFieldBox from '../../dialogs/valueFieldBox'
import DatePickerBox from '../../dialogs/datePickerBox'
import { MAX_DEPOSIT_PC, MAX_RISK_PC, MAX_RISK_REWARD_PC } from '../../../utils/constants'


const positions = [
    { id: 0, name: 'long' },
    { id: 1, name: 'short' }
]

interface Eval {
    outcomeExp: number,
    gainPc: number,
    fees: number,
    riskPc: number,
    riskRewardPc: number,
    breakEven: number,
    volume: number
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
    volume: number
}

const initFormState = (formState: FormState, dispatch: Dispatch<FormAction>, tickerId: number, marketId: number, positionId: number) => {
    if (formState.isInitialized)
        return

    const payload: FormActionPayload = {
        valuesNumeric: [
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
                name: 'volume',
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
    const riskRewardPc = getFieldValue('riskRewardPc', formState) as number
    const riskPc = getFieldValue('riskPc', formState) as number
    const depositPc = getFieldValue('depositPc', formState) as number
    const breakEven = getFieldValue('breakEven', formState) as number
    const fees = getFieldValue('fees', formState) as number
    const note = getFieldValue('note', formState) as string
    const date = getFieldValue('date', formState) as Date
    const levelPrice = getFieldValue('levelPrice', formState) as number
    const atr = getFieldValue('atr', formState) as number
    const volume = getFieldValue('volume', formState) as number
    const gainPc = getFieldValue('volume', formState) as number
    const breakEvenPercentageStr = () => {
        if (breakEven && price)
            return `(${roundTo2(Math.abs(breakEven / (price / 100) - 100))}%)`
        else
            return ''
    }
    // noinspection DuplicatedCode
    const handleEvalToFit = useCallback(async () => {
        if (evaluate && (levelPrice && atr && riskPc && riskRewardPc && depositPc && stopLoss)) {
            setLoading(true)
            const ev: EvalToFit = await postEvalToFit({
                brokerId: currentBroker.id,
                tickerId: Number(tickerId),
                levelPrice,
                atr,
                riskPc,
                riskRewardPc,
                depositPc,
                stopLoss,
                date: date.toISOString(),
                short: positionId == '1'
            })
            setLoading(false)
            dispatch({ type: 'set', payload: { name: 'outcomeExp', valueNum: ev.outcomeExp, valid: true } })
            dispatch({ type: 'set', payload: { name: 'gainPc', valueNum: ev.gainPc, valid: true } })
            dispatch({ type: 'set', payload: { name: 'fees', valueNum: ev.fees, valid: true } })
            dispatch({ type: 'set', payload: { name: 'price', valueNum: ev.price, valid: true } })
            dispatch({ type: 'set', payload: { name: 'riskPc', valueNum: ev.riskPc, valid: true } })
            dispatch({ type: 'set', payload: { name: 'riskRewardPc', valueNum: ev.riskRewardPc, valid: true } })
            dispatch({ type: 'set', payload: { name: 'breakEven', valueNum: ev.breakEven, valid: true } })
            dispatch({ type: 'set', payload: { name: 'stopLoss', valueNum: ev.stopLoss, valid: true } })
            dispatch({ type: 'set', payload: { name: 'takeProfit', valueNum: ev.takeProfit, valid: true } })
            dispatch({ type: 'set', payload: { name: 'items', valueNum: ev.items, valid: true } })
            dispatch({ type: 'set', payload: { name: 'volume', valueNum: ev.volume, valid: true } })

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
        if (evaluate && price && items && stopLoss && atr && takeProfit) {
            console.log('evaluation')
            const ev: Eval = await postEval({
                brokerId: currentBroker.id,
                tickerId: Number(tickerId),
                atr,
                takeProfit,
                price,
                items,
                stopLoss,
                date: date.toISOString(),
                short: positionId == '1'
            })
            // noinspection DuplicatedCode
            dispatch({ type: 'set', payload: { name: 'outcomeExp', valueNum: ev.outcomeExp, valid: true } })
            dispatch({ type: 'set', payload: { name: 'gainPc', valueNum: ev.gainPc, valid: true } })
            dispatch({ type: 'set', payload: { name: 'fees', valueNum: ev.fees, valid: true } })
            dispatch({ type: 'set', payload: { name: 'riskPc', valueNum: ev.riskPc, valid: true } })
            dispatch({ type: 'set', payload: { name: 'riskRewardPc', valueNum: ev.riskRewardPc, valid: true } })
            dispatch({ type: 'set', payload: { name: 'breakEven', valueNum: ev.breakEven, valid: true } })
            dispatch({ type: 'set', payload: { name: 'volume', valueNum: ev.volume, valid: true } })
            return
        }
        if (price && items && stopLoss && riskPc && fees && atr) {
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
                riskPc,
                riskRewardPc,
                depositPc,
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
        atr,
        stopLoss,
        date,
        riskPc,
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
        <DialogContent sx={{ fontSize: remCalc(14), fontFamily: 'sans-serif' }}>
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
                        <DatePickerBox
                            label="Date:"
                            fieldName={'date'}
                            dispatch={dispatch}/>
                        <SelectFieldBox
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
                            label={`Price: (${currentTicker ? (currentTicker.currency.name) : '???'})`}
                            value={price}
                            fieldName={'price'}
                            color={BLUE}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            label={'Level Price:'}
                            value={levelPrice}
                            fieldName={'levelPrice'}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            label={'ATR:'}
                            value={atr}
                            fieldName={'atr'}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            label={'Items:'}
                            value={items}
                            color={BLUE}
                            fieldName={'items'}
                            dispatch={dispatch}/>
                    </Grid>
                </Grid>
                <Grid item xs={1}>
                    <Grid container columns={1}>
                        <NumberFieldBox
                            label={'Vol./depo. (%):'}
                            value={depositPc}
                            color={greaterColor(depositPc, defaultColor, MAX_DEPOSIT_PC)}
                            fieldName={'depositPc'}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            label={'Stop Loss:'}
                            value={stopLoss}
                            color={BLUE}
                            fieldName={'stopLoss'}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            color={takeColor(takeProfit, price, atr)}
                            label={'Take Profit:'}
                            value={takeProfit}
                            fieldName={'takeProfit'}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            label={'Risk(%):'}
                            fieldName={'rickPc'}
                            color={greaterColor(riskPc, defaultColor, MAX_RISK_PC)}
                            value={riskPc}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            label={'R/R (%):'}
                            fieldName={'riskRewardPc'}
                            color={greaterColor(riskRewardPc, defaultColor, MAX_RISK_REWARD_PC)}
                            value={riskRewardPc}
                            dispatch={dispatch}
                        />
                        <ValueFieldBox
                            label={'Out. Exp.:'}
                            value={outcomeExp}/>
                        <ValueFieldBox
                            label={'Gain:'}
                            variant={'pc'}
                            value={gainPc}/>
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
