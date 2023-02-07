// noinspection TypeScriptValidateTypes

import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import Dialog from '@mui/material/Dialog'
import { remCalc } from '../../utils/utils'
import Button from '../button'
import React, { useCallback, useState } from 'react'
import { ButtonContainerStyled, FieldName } from '../../styles/style'
import { Box, Grid, styled } from '@mui/material'
import dayjs, { Dayjs } from 'dayjs'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import TextField from '@mui/material/TextField'
import { PositionCloseType, TradeLog } from 'types'
import NumberInput from '../numberInput'
import { DesktopDateTimePicker } from '@mui/x-date-pickers'

interface CloseDialogProps {
    position: TradeLog,
    isOpen: boolean,
    close: (close: PositionCloseType) => void,
    onClose: () => void
}

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

const NoteBox = styled(Box)(() => ({
    width: '100%',
    marginTop: remCalc(10)
}))

interface DatePickerProps {
    onChange: (date: Date) => void
}

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

const BasicDateTimePicker = ({ onChange }: DatePickerProps) => {
    const [value, setValue] = React.useState<Dayjs | null>(dayjs(new Date()))

    const handleChange = useCallback((value: Dayjs| null) => {
        if (value) {
            setValue(value)
            onChange(value.toDate())
        }
    }, [])

    return (
        <LocalizationProvider dateAdapter={AdapterDayjs}>
            <DesktopDateTimePicker
                renderInput={(props) => <TextFieldStyled {...props} />}
                value={value}
                inputFormat="YYYY-MM-DD HH:mm"
                onChange={handleChange}
                onAccept={handleChange}
            />
        </LocalizationProvider>
    )
}

export default ({ onClose, isOpen, position, close }: CloseDialogProps) => {
    const [priceError, setPriceError] = useState(false)
    const [price, setPrice] = useState<number | undefined>(undefined)
    const [note, setNote] = useState(position.note)
    const [date, setDate] = useState(new Date())

    const validate = (): boolean => {
        if (priceError )
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
                priceClose: price,
                note: note
            })
            onClose()
        }
    }, [price, date,  note ])

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
                                    Price: {`${position.currency.name}`}
                                </FieldName>
                                <FieldValue>
                                    <NumberInput value={price} onChange={priceChangeHandler} onError={priceErrorHandler}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                    </Grid>
                </Grid>
                <Grid item xs={1}>
                    <NoteBox>
                        <TextField
                            id="outlined-textarea"
                            label="Note"
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
