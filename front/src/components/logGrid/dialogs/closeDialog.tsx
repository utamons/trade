// noinspection TypeScriptValidateTypes

import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import Dialog from '@mui/material/Dialog'
import { remCalc } from '../../../utils/utils'
import Button from '../../tools/button'
import React, { Dispatch, useCallback, useEffect } from 'react'
import { ButtonContainerStyled, NoteBox } from '../../../styles/style'
import { Grid } from '@mui/material'
import TextField from '@mui/material/TextField'
import { FormAction, FormActionPayload, FormState, PositionCloseType, TradeLog } from 'types'
import { getFieldErrorText, getFieldValue, isFieldValid, useForm } from '../../dialogs/dialogUtils'
import DatePickerBox from '../../dialogs/datePickerBox'
import NumberFieldBox from '../../dialogs/numberFieldBox'

interface CloseDialogProps {
    position: TradeLog,
    isOpen: boolean,
    close: (close: PositionCloseType) => void,
    onClose: () => void
}

const initFormState = (
    formState: FormState,
    dispatch: Dispatch<FormAction>,
    quantity: number,
    brokerInterest: number | undefined,
    note: string | undefined) => {
    if (formState.isInitialized)
        return

    const payload: FormActionPayload = {
        valuesNumeric: [
            {
                name: 'price',
                valid: true,
                value: undefined
            },
            {
                name: 'quantity',
                valid: true,
                value: quantity
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

export default ({ onClose, isOpen, position, close }: CloseDialogProps) => {
    const { formState, dispatch } = useForm()

    useEffect(() => {
        initFormState(formState, dispatch, position.itemNumber, position.brokerInterest, position.note)
    }, [formState])

    const price = getFieldValue('price', formState) as number
    const quantity = getFieldValue('quantity', formState) as number
    const brokerInterest = getFieldValue('brokerInterest', formState) as number
    const note = getFieldValue('note', formState) as string
    const date = getFieldValue('date', formState) as Date

    const valid = () => {
        let state = true
        if (price == undefined) {
            dispatch({ type: 'set', payload: { name: 'price', valid: false, errorText: 'required' } })
            state = false
        }
        if (quantity == undefined) {
            dispatch({ type: 'set', payload: { name: 'quantity', valid: false, errorText: 'required' } })
            state = false
        }
        if (price <= 0) {
            dispatch({ type: 'set', payload: { name: 'price', valid: false, errorText: 'must be greater than 0' } })
            state = false
        }
        if (quantity <= 0) {
            dispatch({ type: 'set', payload: { name: 'quantity', valid: false, errorText: 'must be greater than 0' } })
            state = false
        }
        return state
    }

    const handleClose = useCallback(async () => {
        if (!valid()) {
            return
        }
        if (price) {
            close({
                id: position.id,
                dateClose: date.toISOString(),
                quantity: quantity ? quantity : position.itemNumber,
                priceClose: price,
                brokerInterest: brokerInterest ? brokerInterest : 0,
                note: note
            })
            onClose()
        }
    }, [price, quantity, date, note, brokerInterest])

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
                            label="Quantity:"
                            value={quantity}
                            valid={isFieldValid('quantity', formState)}
                            errorText={getFieldErrorText('quantity', formState)}
                            fieldName={'quantity'}
                            dispatch={dispatch}/>
                        <NumberFieldBox
                            label={`Price: ${position.currency.name}`}
                            valid={isFieldValid('price', formState)}
                            errorText={getFieldErrorText('price', formState)}
                            value={price}
                            fieldName={'price'}
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
