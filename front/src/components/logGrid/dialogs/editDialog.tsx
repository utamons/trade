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
import { FormAction, FormActionPayload, FormState, PositionEditType, TradeLog } from 'types'
import { getFieldValue, useForm } from '../../dialogs/dialogUtils'
import NumberFieldBox from '../../dialogs/numberFieldBox'

interface EditDialogProps {
    position: TradeLog,
    isOpen: boolean,
    edit: (edit: PositionEditType) => void,
    onClose: () => void
}

const initFormState = (
    formState: FormState,
    dispatch: Dispatch<FormAction>,
    stopLoss: number,
    takeProfit: number | undefined,
    note: string | undefined) => {
    if (formState.isInitialized)
        return

    const payload: FormActionPayload = {
        valuesNumeric: [
            {
                name: 'stopLoss',
                valid: true,
                value: stopLoss
            },
            {
                name: 'takeProfit',
                valid: true,
                value: takeProfit
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

export default ({ onClose, isOpen, position, edit }: EditDialogProps) => {

    const { formState, dispatch } = useForm()

    const { isValid } = formState

    useEffect(() => {
        initFormState(formState, dispatch, position.stopLoss, position.takeProfit, position.note)
    }, [formState])

    const stopLoss = getFieldValue('stopLoss', formState) as number
    const takeProfit = getFieldValue('takeProfit', formState) as number | undefined
    const note = getFieldValue('note', formState) as string | undefined

    const handleSubmit = useCallback(async () => {
        if (!isValid) {
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
    }, [stopLoss, note, takeProfit, isValid])

    return <Dialog
        maxWidth={false}
        open={isOpen}
    >
        <DialogContent sx={{ fontSize: remCalc(12), fontFamily: 'sans' }}>
            <Grid sx={{ width: remCalc(350) }} container columns={1}>
                <Grid item xs={1}>
                    <Grid container columns={1}>
                        <NumberFieldBox
                            fieldName={'stopLoss'}
                            label={'StopLoss:'}
                            value={stopLoss}
                            dispatch={dispatch} />
                        <NumberFieldBox
                            fieldName={'takeProfit'}
                            label={'Take Profit:'}
                            value={takeProfit}
                            dispatch={dispatch} />
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
                <Button style={{ minWidth: remCalc(101) }} text="Submit" onClick={handleSubmit}/>
                <Button style={{ minWidth: remCalc(101) }} text="Cancel" onClick={onClose}/>
            </ButtonContainerStyled>
        </DialogActions>
    </Dialog>
}
