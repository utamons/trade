// noinspection TypeScriptValidateTypes

import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import Dialog from '@mui/material/Dialog'
import { remCalc } from '../../utils/utils'
import Button from '../tools/button'
import React, { Dispatch, useCallback } from 'react'
import { ButtonContainerStyled } from '../../styles/style'
import { Box, styled } from '@mui/material'
import { ExchangeDialogProps, FormAction, FormActionPayload, FormState } from 'types'
import Typography from '@mui/material/Typography'
import Select from '../tools/select'
import NumberInput from '../tools/numberInput'
import { getFieldValue, useForm } from './dialogUtils'

const ContainerStyled = styled(Box)(() => ({
    display: 'flex',
    flexFlow: 'column',
    fontSize: remCalc(12),
    justifyContent: 'space-between',
    padding: remCalc(8),
    gap: remCalc(20)
}))

const SelectBoxStyled = styled(Box)(() => ({
    display: 'flex',
    flexFlow: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    width: remCalc(160)
}))

const initFormState = (formState: FormState, dispatch: Dispatch<FormAction>, currencyId: number) => {
    if (formState.isInitialized)
        return

    const payload: FormActionPayload = {
        valuesNumeric: [
            {
                name: 'currencyFromId',
                valid: true,
                value: currencyId
            },
            {
                name: 'currencyToId',
                valid: true,
                value: currencyId
            },
            {
                name: 'valueFrom',
                valid: true,
                value: undefined
            },
            {
                name: 'valueTo',
                valid: true,
                value: undefined
            }
        ]
    }

    dispatch({ type: 'init', payload })
}


export default ({ open, onExchange, onCancel, currencies }: ExchangeDialogProps) => {
    const { formState, dispatch } = useForm()

    initFormState(formState, dispatch, currencies ? currencies[0].id : 0)

    const { isValid } = formState

    const valueFrom = getFieldValue('valueFrom', formState) as number | undefined
    const valueTo = getFieldValue('valueTo', formState) as number | undefined
    const currencyFromId = '' + getFieldValue('currencyFromId', formState)
    const currencyToId = '' + getFieldValue('currencyToId', formState)

    const handleExchange = useCallback(() => {
        if (!valueTo || !valueFrom)
            return
        if (valueFrom <= 0 || valueTo <=0 || !isValid)
            return
        onExchange(Number(currencyFromId),  Number(currencyToId), valueFrom, valueTo)
    }, [valueFrom, valueTo, isValid, currencyFromId, currencyToId])

    return <Dialog open={open}>
        <DialogContent>
            <ContainerStyled>
                <SelectBoxStyled>
                    <Typography variant="caption">From:</Typography>
                    <Select
                        items={currencies ? currencies : []}
                        value={currencyFromId}
                        name='currencyFromId'
                        dispatch={dispatch}
                        variant="medium"
                    />
                </SelectBoxStyled>
                <SelectBoxStyled>
                    <Typography variant="caption">To:</Typography>
                    <Select
                        items={currencies ? currencies : []}
                        value={currencyToId}
                        name='currencyToId'
                        dispatch={dispatch}
                        variant="medium"
                    />
                </SelectBoxStyled>
                <NumberInput name='valueFrom' dispatch={dispatch} value={valueFrom} label="Amount from" />
                <NumberInput name='valueTo' dispatch={dispatch} value={valueTo} label="Amount to" />
            </ContainerStyled>
        </DialogContent>

        <DialogActions sx={{ justifyContent: 'center' }}>
            <ButtonContainerStyled>
                <Button style={{ minWidth: remCalc(101) }} text="Exchange" onClick={handleExchange}/>
                <Button style={{ minWidth: remCalc(101) }} text="Cancel" onClick={onCancel}/>
            </ButtonContainerStyled>
        </DialogActions>
    </Dialog>

}
