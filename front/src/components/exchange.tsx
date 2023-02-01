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
import { ExchangeDialogProps } from 'types'
import Typography from '@mui/material/Typography'
import Select from './select'
import NumberInput from './numberInput'

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
    const [valueFrom, setValueFrom] = useState(0)
    const [valueTo, setValueTo] = useState(0)
    const [errorFrom, setErrorFrom] = useState(false)
    const [errorTo, setErrorTo] = useState(false)

    const handleExchange = useCallback(() => {
        if (valueFrom <= 0 || valueTo <=0 || errorFrom || errorTo)
            return
        onExchange(Number(currencyFromId),  Number(currencyToId), Number(valueFrom), Number(valueTo))
    }, [valueFrom, valueTo, errorFrom, errorTo, currencyFromId, currencyToId])

    const handleFromSelector = useCallback((event: SelectChangeEvent<unknown>) => {
        setCurrencyFromId(event.target.value as string)
    }, [])

    const handleToSelector = useCallback((event: SelectChangeEvent<unknown>) => {
        setCurrencyToId(event.target.value as string)
    }, [])

    return <Dialog open={open}>
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
                <NumberInput label="Amount from" onChange={setValueFrom} onError={setErrorFrom}/>
                <NumberInput label="Amount to" onChange={setValueTo} onError={setErrorTo}/>
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
