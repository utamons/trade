import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import DialogTitle from '@mui/material/DialogTitle'
import Dialog from '@mui/material/Dialog'
import { remCalc } from '../../utils/utils'
import Button from '../tools/button'
import React, { Dispatch, useCallback } from 'react'
import { ButtonContainerStyled } from '../../styles/style'
import { Box, styled } from '@mui/material'
import { FormAction, FormActionPayload, FormState, RefillDialogProps } from 'types'
import Select from '../tools/select'
import NumberInput from '../tools/numberInput'
import { getFieldErrorText, getFieldValue, isFieldValid, useForm } from './dialogUtils'

const ContainerStyled = styled(Box)(() => ({
    display: 'flex',
    flexFlow: 'column',
    justifyContent: 'space-between',
    padding: remCalc(8),
    gap: remCalc(20)
}))

const DialogTitleStyled = styled(DialogTitle)(() => ({
    fontSize: remCalc(16),
    fontWeight: 501
}))

const initFormState = (formState: FormState, dispatch: Dispatch<FormAction>, currencyId: number) => {
    if (formState.isInitialized)
        return

    const payload: FormActionPayload = {
        values: [
            {
                name: 'currencyId',
                valid: true,
                value: currencyId
            },
            {
                name: 'value',
                valid: true,
                value: undefined
            }
        ]
    }

    dispatch({ type: 'init', payload })
}

export default ({ open, title, onSubmit, onCancel, currencies }: RefillDialogProps) => {
    const { formState, dispatch } = useForm()

    initFormState(formState, dispatch, currencies ? currencies[0].id : 0)

    const currencyId = '' + getFieldValue('currencyId', formState)
    const value = getFieldValue('value', formState) as number | undefined

    const handleSubmit = useCallback(() => {
        if (value == undefined) {
            dispatch({ type: 'set', payload: { name: 'value', valid: false, errorText: 'required' } })
            return
        }
        onSubmit(Number(currencyId), value)
    }, [value, currencyId])

    return <Dialog
        open={open}
    >
        <DialogTitleStyled>
            {title}
        </DialogTitleStyled>
        <DialogContent>
            <ContainerStyled>
                <Select
                    items={currencies ? currencies : []}
                    value={currencyId}
                    variant="medium"
                    name={'currencyId'}
                    dispatch={dispatch}
                />
                <NumberInput value={value}
                             valid={isFieldValid('value', formState)}
                             errorText={getFieldErrorText('value', formState)}
                             label={'Amount'}
                             name={'value'}
                             dispatch={dispatch}
                            />
            </ContainerStyled>
        </DialogContent>

        <DialogActions sx={{ justifyContent: 'center' }}>
            <ButtonContainerStyled>
                <Button style={{ minWidth: remCalc(101) }} text="Submit" onClick={handleSubmit}/>
                <Button style={{ minWidth: remCalc(101) }} text="Cancel" onClick={onCancel}/>
            </ButtonContainerStyled>
        </DialogActions>
    </Dialog>

}
