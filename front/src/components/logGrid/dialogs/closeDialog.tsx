// noinspection TypeScriptValidateTypes

import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import Dialog from '@mui/material/Dialog'
import { remCalc } from '../../../utils/utils'
import Button from '../../tools/button'
import React, { Dispatch, useCallback, useEffect } from 'react'
import { ButtonContainerStyled, FieldBox, FieldName, NoteBox } from '../../../styles/style'
import { Grid } from '@mui/material'
import TextField from '@mui/material/TextField'
import { FormAction, FormActionPayload, FormState, PositionCloseType, TradeLog } from 'types'
import NumberInput from '../../tools/numberInput'
import { BasicDateTimePicker } from '../../tools/dateTimePicker'
import { FieldValue } from './openDialog'
import { getFieldValue, useForm } from '../../dialogs/dialogUtils'

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

    const { isValid } = formState

    useEffect(() => {
        initFormState(formState, dispatch, position.itemNumber, position.brokerInterest, position.note)
    }, [formState])

    const price = getFieldValue('price', formState) as number
    const quantity = getFieldValue('quantity', formState) as number
    const brokerInterest = getFieldValue('brokerInterest', formState) as number
    const note = getFieldValue('note', formState) as string
    const date = getFieldValue('date', formState) as Date

    const handleClose = useCallback(async () => {
        if (!isValid) {
            console.error('validation failed')
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
    }, [price, date, note, brokerInterest, isValid])

    return <Dialog
        maxWidth={false}
        open={isOpen}
    >
        <DialogContent sx={{ fontSize: remCalc(12), fontFamily: 'sans' }}>
            <Grid sx={{ width: remCalc(350) }} container columns={1}>
                <Grid item xs={1}>
                    <Grid container columns={1}>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Date Close:</FieldName>
                                <FieldValue>
                                    <BasicDateTimePicker name={'date'} dispatch={dispatch}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>
                                    Quantity:
                                </FieldName>
                                <FieldValue>
                                    <NumberInput value={quantity} name={'quantity'} dispatch={dispatch} />
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>
                                    Price: {`${position.currency.name}`}
                                </FieldName>
                                <FieldValue>
                                    <NumberInput value={price} dispatch={dispatch} name={'price'}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        {position.position == 'short' ? <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>
                                    Broker interest (USD):
                                </FieldName>
                                <FieldValue>
                                    <NumberInput value={brokerInterest} zeroAllowed
                                                 name={'brokerInterest'} dispatch={dispatch}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid> : <></>}
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