// noinspection TypeScriptValidateTypes

import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import Dialog from '@mui/material/Dialog'
import { remCalc } from '../utils/utils'
import Button from './button'
import React, { useCallback, useState } from 'react'
import { ButtonContainerStyled } from '../styles/style'
import Select, { SelectChangeEvent } from '@mui/material/Select'
import MenuItem from '@mui/material/MenuItem'
import FormControl from '@mui/material/FormControl'
import { Box, styled } from '@mui/material'
import TextField from '@mui/material/TextField'
import { RefillDialogProps } from 'types'

const ContainerStyled = styled(Box)(() => ({
    display: 'flex',
    flexFlow: 'column',
    justifyContent: 'space-between',
    padding: remCalc(8),
    gap: remCalc(20)
}))

export default ({ open, onRefill, onCancel, currencies }: RefillDialogProps) => {
    const [currencyId, setCurrencyId] = useState('' + (currencies ? currencies[0].id : 0))
    const [value, setValue] = useState('')
    const [error, setError] = useState(false)
    const [errorText, setErrorText] = useState('')

    const handleRefill = useCallback(() => {
        if (validate(value))
            return
        onRefill(Number(currencyId), Number(value))
    }, [value])

    const handleSelector = useCallback((event: SelectChangeEvent) => {
        setCurrencyId(event.target.value as string)
    }, [])

    const handleInput = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
        setValue(event.target.value)
    }, [])

    const validate = (value: string): boolean => {
        const num = Number(value)
        if (!value || isNaN(num)) {
            setErrorText('Incorrect value')
            setError(true)
            return true
        }
        setError(false)
        setErrorText('')
        return false
    }

    const handleValidate: React.FocusEventHandler<HTMLInputElement | HTMLTextAreaElement> =
        useCallback((event: React.FocusEvent<HTMLInputElement | HTMLTextAreaElement>) => {
            validate(event.target.value)
        }, [])

    function getCurrencies() {
        return currencies ?
            currencies.map(
                currency => <MenuItem key={currency.id}
                    value={currency.id}>{currency.name}
                </MenuItem>) : <></>
    }

    return <Dialog
        open={open}
    >
        <DialogContent>
            <ContainerStyled>
                <FormControl variant="standard">
                    <Select
                        value={currencyId}
                        onChange={handleSelector}
                    >
                        {getCurrencies()}
                    </Select>
                </FormControl>
                <TextField
                    label="Amount"
                    variant="standard"
                    onChange={handleInput}
                    onBlur={handleValidate}
                    error={error}
                    helperText={errorText}
                    inputProps={{ inputMode: 'numeric' }}
                />
            </ContainerStyled>
        </DialogContent>

        <DialogActions sx={{ justifyContent: 'center' }}>
            <ButtonContainerStyled>
                <Button style={{ minWidth: remCalc(101) }} text="Refill" onClick={handleRefill}/>
                <Button style={{ minWidth: remCalc(101) }} text="Cancel" onClick={onCancel}/>
            </ButtonContainerStyled>
        </DialogActions>
    </Dialog>

}
