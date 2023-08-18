// noinspection TypeScriptValidateTypes

import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import Dialog from '@mui/material/Dialog'
import { remCalc } from '../../utils/utils'
import Button from '../tools/button'
import React, { Dispatch, useCallback, useContext, useEffect } from 'react'
import { ButtonContainerStyled, NoteBox } from '../../styles/style'
import { Grid } from '@mui/material'
import TextField from '@mui/material/TextField'
import { FormAction, FormActionPayload, FormState, TradeLog } from 'types'
import { getFieldErrorText, getFieldValue, isFieldValid, useForm } from './dialogUtils'
import DatePickerBox from './components/datePickerBox'
import NumberFieldBox from './components/numberFieldBox'
import { TradeContext } from '../../trade-context'

interface CloseDialogProps {
    position: TradeLog,
    isOpen: boolean,
    onClose: () => void
}

const initFormState = (
    formState: FormState,
    dispatch: Dispatch<FormAction>,
    quantity: number,
    total: number,
    stopLoss: number,
    takeProfit: number,
    brokerInterest: number | undefined,
    note: string | undefined) => {
    if (formState.isInitialized)
        return

    const payload: FormActionPayload = {
        valuesNumeric: [
            {
                name: 'items',
                valid: true,
                value: quantity
            },
            {
                name: 'total',
                valid: true,
                value: total
            },
            {
                name: 'closeCommission',
                valid: true,
                value: undefined
            },
            {
                name: 'finalStopLoss',
                valid: true,
                value: stopLoss
            },
            {
                name: 'finalTakeProfit',
                valid: true,
                value: takeProfit
            },
            {
                name: 'brokerInterest',
                valid: true,
                value: brokerInterest
            }
        ],
        valuesString: [
            {
                name: 'note',
                valid: true,
                value: note
            }
        ],
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

export default ({ onClose, isOpen, position }: CloseDialogProps) => {
    const { close } = useContext(TradeContext)
    const { formState, dispatch } = useForm()

    useEffect(() => {
        let quantity = position.itemBought ?? position.itemSold ?? 0
        if (position.itemBought != undefined && position.itemSold != undefined) {
            quantity = Math.abs(position.itemBought - position.itemSold)
        }
        let total = position.totalBought ?? position.totalSold ?? 0
        if (position.totalBought != undefined && position.totalSold != undefined) {
            total = Math.abs(position.totalBought - position.totalSold)
        }

        initFormState(formState, dispatch,
            quantity,
            total,
            position.openStopLoss,
            position.openTakeProfit,
            position.brokerInterest, position.note)
    }, [formState])

    const items = getFieldValue('items', formState) as number
    const total = getFieldValue('total', formState) as number
    const brokerInterest = getFieldValue('brokerInterest', formState) as number
    const note = getFieldValue('note', formState) as string
    const date = getFieldValue('date', formState) as Date
    const finalStopLoss = getFieldValue('finalStopLoss', formState) as number
    const finalTakeProfit = getFieldValue('finalTakeProfit', formState) as number
    const closeCommission = getFieldValue('closeCommission', formState) as number

    const valid = () => {
        let state = true
        if (date == undefined) {
            dispatch({ type: 'set', payload: { name: 'date', valid: false, errorText: 'required' } })
            state = false
        }
        if (items == undefined) {
            dispatch({ type: 'set', payload: { name: 'items', valid: false, errorText: 'required' } })
            state = false
        }
        if (total == undefined) {
            dispatch({ type: 'set', payload: { name: 'total', valid: false, errorText: 'required' } })
            state = false
        }
        if (items <= 0) {
            dispatch({ type: 'set', payload: { name: 'items', valid: false, errorText: 'must be greater than 0' } })
            state = false
        }
        if (total <= 0) {
            dispatch({ type: 'set', payload: { name: 'total', valid: false, errorText: 'must be greater than 0' } })
            state = false
        }
        if (closeCommission == undefined) {
            dispatch({ type: 'set', payload: { name: 'closeCommission', valid: false, errorText: 'required' } })
            state = false
        }
        if (closeCommission <= 0) {
            dispatch({ type: 'set', payload: { name: 'closeCommission', valid: false, errorText: 'must be greater than 0' } })
            state = false
        }
        if (finalStopLoss == undefined) {
            dispatch({ type: 'set', payload: { name: 'finalStopLoss', valid: false, errorText: 'required' } })
            state = false
        }
        if (finalTakeProfit == undefined) {
            dispatch({ type: 'set', payload: { name: 'finalTakeProfit', valid: false, errorText: 'required' } })
            state = false
        }
        return state
    }

    const handleClose = useCallback(async () => {
        if (!valid()) {
            return
        }
            close({
                id: position.id,
                dateClose: date.toISOString(),
                itemBought: position.position == 'long' ? undefined : items,
                itemSold: position.position == 'short' ? undefined : items,
                totalBought: position.position == 'long' ? undefined : total,
                totalSold: position.position == 'short' ? undefined : total,
                brokerInterest: brokerInterest ? brokerInterest : 0,
                closeCommission: closeCommission,
                finalStopLoss: finalStopLoss,
                finalTakeProfit: finalTakeProfit,
                note: note
            })
            onClose()
    }, [total, items, date, note, brokerInterest, closeCommission, finalStopLoss, finalTakeProfit])

    return <Dialog
        maxWidth={false}
        open={isOpen}
    >
        <DialogContent sx={{ fontSize: remCalc(12), fontFamily: 'sans' }}>
            <Grid sx={{ width: remCalc(350) }} container columns={1}>
                <Grid item xs={1}>
                    <Grid container columns={1}>
                        <DatePickerBox
                            label="Date close:"
                            fieldName={'date'}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            label="Items:"
                            value={items}
                            valid={isFieldValid('items', formState)}
                            errorText={getFieldErrorText('items', formState)}
                            fieldName={'items'}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            label={`Total: ${position.currency.name}`}
                            valid={isFieldValid('total', formState)}
                            errorText={getFieldErrorText('total', formState)}
                            value={total}
                            fieldName={'total'}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            label={'Close comm: USD'}
                            valid={isFieldValid('closeCommission', formState)}
                            errorText={getFieldErrorText('closeCommission', formState)}
                            value={closeCommission}
                            fieldName={'closeCommission'}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            label={`Final stop: ${position.currency.name}`}
                            valid={isFieldValid('finalStopLoss', formState)}
                            errorText={getFieldErrorText('finalStopLoss', formState)}
                            value={finalStopLoss}
                            fieldName={'finalStopLoss'}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            label={`Final take: ${position.currency.name}`}
                            valid={isFieldValid('finalTakeProfit', formState)}
                            errorText={getFieldErrorText('finalTakeProfit', formState)}
                            value={finalTakeProfit}
                            fieldName={'finalTakeProfit'}
                            dispatch={dispatch}/>
                        {position.position == 'short' ?
                            <NumberFieldBox
                                label={`Broker interest: ${position.currency.name}`}
                                value={brokerInterest}
                                fieldName={'brokerInterest'}
                                zeroAllowed
                                dispatch={dispatch}/> : <></>}
                    </Grid>
                </Grid>
                <Grid item xs={1}>
                    <NoteBox>
                        <TextField
                            id="outlined-textarea"
                            label="Note"
                            value={note}
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
                <Button style={{ minWidth: remCalc(101) }} text="Close" onClick={handleClose}/>
                <Button style={{ minWidth: remCalc(101) }} text="Cancel" onClick={onClose}/>
            </ButtonContainerStyled>
        </DialogActions>
    </Dialog>
}
