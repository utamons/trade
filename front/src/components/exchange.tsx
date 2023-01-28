// noinspection TypeScriptValidateTypes

import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import Dialog from '@mui/material/Dialog'
import { remCalc } from '../utils/utils'
import Button from './button'
import React, { useCallback, useState } from 'react'
import { ButtonContainerStyled } from '../styles/style'
import { SelectChangeEvent } from '@mui/material/Select'
import { Box, styled } from '@mui/material'
import TextField from '@mui/material/TextField'
import { ExchangeDialogProps } from 'types'
import Typography from '@mui/material/Typography'
import Select from './select'

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

export default ({ open, onExchange, onCancel, currencies }: ExchangeDialogProps) => {
    const [currencyFromId, setCurrencyFromId] = useState('' + (currencies ? currencies[0].id : 0))
    const [currencyToId, setCurrencyToId] = useState('' + (currencies ? currencies[1].id : 0))
    const [valueFrom, setValueFrom] = useState('')
    const [valueTo, setValueTo] = useState('')
    const [errorFrom, setErrorFrom] = useState(false)
    const [errorFromText, setErrorFromText] = useState('')
    const [errorTo, setErrorTo] = useState(false)
    const [errorToText, setErrorToText] = useState('')

    const handleExchange = useCallback(() => {
        if (validate(valueFrom, valueTo))
            return
        onExchange(Number(currencyFromId),  Number(currencyToId), Number(valueFrom), Number(valueTo))
    }, [valueFrom, valueTo, currencyFromId, currencyToId])

    const handleFromSelector = useCallback((event: SelectChangeEvent<unknown>) => {
        setCurrencyFromId(event.target.value as string)
    }, [])

    const handleToSelector = useCallback((event: SelectChangeEvent<unknown>) => {
        setCurrencyToId(event.target.value as string)
    }, [])

    const handleFromInput = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
        setValueFrom(event.target.value)
    }, [])

    const handleToInput = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
        setValueTo(event.target.value)
    }, [])

    const validateFrom = (valueFrom: string): boolean => {
        const numFrom = Number(valueFrom)
        if (!valueFrom || isNaN(numFrom)) {
            setErrorFromText('Incorrect value')
            setErrorFrom(true)
            return true
        }

        setErrorFrom(false)
        setErrorFromText('')
        return false
    }

    const validateTo = (valueTo: string): boolean => {
        const numTo = Number(valueTo)
        if (!valueTo || isNaN(numTo)) {
            setErrorToText('Incorrect value')
            setErrorTo(true)
            return true
        }

        setErrorTo(false)
        setErrorToText('')
        return false
    }

    const validate = (valueFrom: string, valueTo: string): boolean => {
        return validateFrom(valueFrom) && validateTo(valueTo)
    }

    const handleValidateFrom: React.FocusEventHandler<HTMLInputElement | HTMLTextAreaElement> =
        useCallback((event: React.FocusEvent<HTMLInputElement | HTMLTextAreaElement>) => {
            validateFrom(event.target.value)
        }, [])

    const handleValidateTo: React.FocusEventHandler<HTMLInputElement | HTMLTextAreaElement> =
        useCallback((event: React.FocusEvent<HTMLInputElement | HTMLTextAreaElement>) => {
            validateTo(event.target.value)
        }, [])

    return <Dialog
        open={open}
    >
        <DialogContent>
            <ContainerStyled>
                <SelectBoxStyled>
                    <Typography variant="caption">From:</Typography>
                    <Select
                        items={currencies ? currencies : []}
                        value={currencyFromId}
                        onChange={handleFromSelector}
                        variant="medium"
                    />
                </SelectBoxStyled>
                <SelectBoxStyled>
                    <Typography variant="caption">To:</Typography>
                    <Select
                        items={currencies ? currencies : []}
                        value={currencyToId}
                        onChange={handleToSelector}
                        variant="medium"
                    />
                </SelectBoxStyled>
                <TextField
                    label="Amount from"
                    variant="standard"
                    onChange={handleFromInput}
                    onBlur={handleValidateFrom}
                    error={errorFrom}
                    helperText={errorFromText}
                    inputProps={{ inputMode: 'numeric' }}
                />
                <TextField
                    label="Amount to"
                    variant="standard"
                    onChange={handleToInput}
                    onBlur={handleValidateTo}
                    error={errorTo}
                    helperText={errorToText}
                    inputProps={{ inputMode: 'numeric' }}
                />
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
