// noinspection TypeScriptValidateTypes

import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import Dialog from '@mui/material/Dialog'
import { remCalc } from '../../utils/utils'
import Button from '../button'
import React, { useCallback, useState } from 'react'
import { ButtonContainerStyled, FieldName } from '../../styles/style'
import { Box, Grid, styled } from '@mui/material'
import TextField from '@mui/material/TextField'
import { PositionEditType, TradeLog } from 'types'
import NumberInput from '../numberInput'

interface EditDialogProps {
    position: TradeLog,
    isOpen: boolean,
    edit: (edit: PositionEditType) => void,
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

export default ({ onClose, isOpen, position, edit }: EditDialogProps) => {
    const [stopLossError, setStopLossError] = useState(false)
    const [stopLoss, setStopLoss] = useState(position.stopLoss)
    const [takeProfitError, setTakeProfitError] = useState(false)
    const [takeProfit, setTakeProfit] = useState(position.takeProfit)
    const [note, setNote] = useState(position.note)

    const validate = (): boolean => {
        if (stopLossError || takeProfitError)
            return true
        return stopLoss == undefined

    }

    const handleClose = useCallback(async () => {
        if (validate()) {
            console.error('validation failed')
            return
        }
        if (stopLoss) {
            edit({
                id: position.id,
                stopLoss,
                takeProfit,
                note: note
            })
            onClose()
        }
    }, [stopLoss, note, takeProfit, takeProfitError, stopLossError])

    const noteChangeHandler = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
        setNote(event.target.value)
    }, [])

    const stopLossChangeHandler = useCallback((newStopLoss: number) => {
        setStopLoss(newStopLoss)
    }, [])

    const stopLossErrorHandler = useCallback((error: boolean) => {
        setStopLossError(error)
    }, [])

    const takeProfitChangeHandler = useCallback((newTakeProfit: number) => {
        setTakeProfit(newTakeProfit)
    }, [])

    const takeProfitErrorHandler = useCallback((error: boolean) => {
        setTakeProfitError(error)
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
                                <FieldName>
                                    StopLoss:
                                </FieldName>
                                <FieldValue>
                                    <NumberInput value={stopLoss} onChange={stopLossChangeHandler}
                                                 onError={stopLossErrorHandler}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>
                                    Take profit:
                                </FieldName>
                                <FieldValue>
                                    <NumberInput value={takeProfit} zeroAllowed onChange={takeProfitChangeHandler}
                                                 onError={takeProfitErrorHandler}/>
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
