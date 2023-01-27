// noinspection TypeScriptValidateTypes

import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import Dialog from '@mui/material/Dialog'
import { remCalc } from '../utils/utils'
import Button from './button'
import React, { useState } from 'react'
import { ButtonContainerStyled } from '../styles/style'
import Select, { SelectChangeEvent } from '@mui/material/Select'
import MenuItem from '@mui/material/MenuItem'
import FormControl from '@mui/material/FormControl'
import { Box, styled } from '@mui/material'
import TextField from '@mui/material/TextField';

const ContainerStyled = styled(Box)(({ theme }) => ({
    display: 'flex',
    flexFlow: 'column',
    justifyContent: 'space-between',
    padding: remCalc(8)
}))

export default ({ open, onClose, currencies }) => {
    const [currencyId, setCurrencyId] = useState('' + (currencies ? currencies[0].id : 0))
    const [value, setValue] = useState('')
    const [error, setError] = useState(false)
    const [errorText, setErrorText] = useState('')

    const handleRefill = () => {
        if (validate(value))
            return
        onClose(currencyId, Number(value))
    }

    const handleSelector = (event: SelectChangeEvent) => {
        setCurrencyId(event.target.value as string);
    };

    const handleInput = (event) => {
        setValue(event.target.value);
    };


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

    const handleValidate = (event) => {
        validate(event.target.value)
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
                        {
                            currencies ? currencies.map(currency => <MenuItem key={currency.id} value={currency.id}>{currency.name}</MenuItem>) : <></>
                        }
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

        <DialogActions sx={{justifyContent: 'center'}}>
            <ButtonContainerStyled>
                <Button style={{ minWidth: remCalc(101) }} text="Refill" onClick={handleRefill}/>
                <Button style={{ minWidth: remCalc(101) }} text="Cancel" onClick={onClose}/>
            </ButtonContainerStyled>
        </DialogActions>
    </Dialog>

}
