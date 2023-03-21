// noinspection TypeScriptValidateTypes

import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import Dialog from '@mui/material/Dialog'
import { remCalc } from '../../../utils/utils'
import Button from '../../tools/button'
import React, { useCallback, useState } from 'react'
import { ButtonContainerStyled, FieldBox, FieldName, NoteBox } from '../../../styles/style'
import { Grid } from '@mui/material'
import TextField from '@mui/material/TextField'
import { PositionCloseType, TradeLog } from 'types'
import NumberInput from '../../tools/numberInput'
import { BasicDateTimePicker } from '../../tools/dateTimePicker'
import { FieldValue } from './openDialog'

interface CloseDialogProps {
    position: TradeLog,
    isOpen: boolean,
    close: (close: PositionCloseType) => void,
    onClose: () => void
}

export default ({ onClose, isOpen, position, close }: CloseDialogProps) => {
    const [priceError, setPriceError] = useState(false)
    const [price, setPrice] = useState<number | undefined>(undefined)
    const [quantityError, setQuantityError] = useState(false)
    const [quantity, setQuantity] = useState<number | undefined>(position.itemNumber)
    const [brokerInterestError, setBrokerInterestError] = useState(false)
    const [brokerInterest, setBrokerInterest] = useState(position.brokerInterest)
    const [note, setNote] = useState(position.note)
    const [date, setDate] = useState(new Date())

    const validate = (): boolean => {
        if (priceError || brokerInterestError || quantityError)
            return true
        return price == undefined

    }

    const handleClose = useCallback(async () => {
        if (validate()) {
            console.error('validation failed')
            return
        }
        if (price) {
            close({
                id: position.id,
                dateClose: date.toISOString(),
                quantity: quantity ? quantity : position.itemNumber,
                priceClose: price,
                brokerInterest: brokerInterest? brokerInterest : 0,
                note: note
            })
            onClose()
        }
    }, [price, date, note, brokerInterest, brokerInterestError, priceError])

    const noteChangeHandler = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
        setNote(event.target.value)
    }, [])

    const dateChangeHandler = useCallback((newDate: Date) => {
        console.log('New date:', newDate)
        setDate(newDate)
    }, [])

    const priceChangeHandler = useCallback((newPrice: number) => {
        setPrice(newPrice)
    }, [])

    const priceErrorHandler = useCallback((error: boolean) => {
        setPriceError(error)
    }, [])

    const quantityChangeHandler = useCallback((newQuantity: number) => {
        setQuantity(newQuantity)
    }, [])

    const quantityErrorHandler = useCallback((error: boolean) => {
        setQuantityError(error)
    }, [])

    const brokerInterestChangeHandler = useCallback((newBrokerInterest: number) => {
        setBrokerInterest(newBrokerInterest)
    }, [])

    const brokerInterestErrorHandler = useCallback((error: boolean) => {
        setBrokerInterestError(error)
    }, [])

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
                                    <BasicDateTimePicker onChange={dateChangeHandler}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>
                                    Quantity:
                                </FieldName>
                                <FieldValue>
                                    <NumberInput value={quantity} onChange={quantityChangeHandler}
                                                 onError={quantityErrorHandler}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>
                                    Price: {`${position.currency.name}`}
                                </FieldName>
                                <FieldValue>
                                    <NumberInput value={price} onChange={priceChangeHandler}
                                                 onError={priceErrorHandler}/>
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
                                                 onChange={brokerInterestChangeHandler}
                                                 onError={brokerInterestErrorHandler}/>
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
                            onChange={noteChangeHandler}
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
