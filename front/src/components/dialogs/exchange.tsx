// noinspection TypeScriptValidateTypes

import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import Dialog from '@mui/material/Dialog'
import { remCalc } from '../../utils/utils'
import Button from '../tools/button'
import React, { Dispatch, useCallback, useContext, useState } from 'react'
import { ButtonContainerStyled } from '../../styles/style'
import { Box, styled } from '@mui/material'
import { ExchangeDialogProps, FormAction, FormActionPayload, FormState } from 'types'
import Typography from '@mui/material/Typography'
import Select from '../tools/select'
import NumberInput from '../tools/numberInput'
import { getFieldErrorText, getFieldValue, isFieldValid, useForm } from './dialogUtils'
import Alert from '@mui/material/Alert'
import { TradeContext } from '../../trade-context'

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
    const { all } = useContext(TradeContext)
    const apiError = all?.apiError
    const [error, setError] = useState<string | undefined>(undefined)
    const { formState, dispatch } = useForm()

    initFormState(formState, dispatch, currencies ? currencies[0].id : 0)

    const { isValid } = formState

    const valueFrom = getFieldValue('valueFrom', formState) as number | undefined
    const valueTo = getFieldValue('valueTo', formState) as number | undefined
    const currencyFromId = '' + getFieldValue('currencyFromId', formState)
    const currencyToId = '' + getFieldValue('currencyToId', formState)

    const handleExchange = useCallback(() => {
        if (valueTo == undefined) {
            dispatch({ type: 'set', payload: { name: 'valueTo', valid: false, errorText: 'required' } })
        }
        if (valueFrom == undefined) {
            dispatch({ type: 'set', payload: { name: 'valueFrom', valid: false, errorText: 'required' } })
        }
        if (valueTo != undefined && valueTo <= 0) {
            dispatch({ type: 'set', payload: { name: 'valueTo', valid: false, errorText: 'must be > 0' } })
        }
        if (valueFrom != undefined && valueFrom <= 0) {
            dispatch({ type: 'set', payload: { name: 'valueFrom', valid: false, errorText: 'must be > 0' } })
        }
        if (valueTo == undefined || valueFrom == undefined || valueTo <= 0 || valueFrom <= 0 || !isValid)
            return
        onExchange(Number(currencyFromId), Number(currencyToId), valueFrom, valueTo)
    }, [valueFrom, valueTo, isValid, currencyFromId, currencyToId])

    return <Dialog open={open}>
        <DialogContent>
            <ContainerStyled>
                <SelectBoxStyled>
                    <Typography variant="caption">From:</Typography>
                    <Select
                        items={currencies ? currencies : []}
                        value={currencyFromId}
                        name="currencyFromId"
                        dispatch={dispatch}
                        variant="medium"
                    />
                </SelectBoxStyled>
                <SelectBoxStyled>
                    <Typography variant="caption">To:</Typography>
                    <Select
                        items={currencies ? currencies : []}
                        value={currencyToId}
                        name="currencyToId"
                        dispatch={dispatch}
                        variant="medium"
                    />
                </SelectBoxStyled>
                <NumberInput
                    name="valueFrom"
                    valid={isFieldValid('valueFrom', formState)}
                    errorText={getFieldErrorText('valueFrom', formState)}
                    dispatch={dispatch}
                    value={valueFrom}
                    label="Amount from"/>
                <NumberInput
                    name="valueTo"
                    valid={isFieldValid('valueTo', formState)}
                    errorText={getFieldErrorText('valueTo', formState)}
                    dispatch={dispatch}
                    value={valueTo}
                    label="Amount to"/>
                <Alert severity="error">{error}</Alert>
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
