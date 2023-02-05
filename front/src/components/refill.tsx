import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import Dialog from '@mui/material/Dialog'
import { remCalc } from '../utils/utils'
import Button from './button'
import React, { useCallback, useState } from 'react'
import { ButtonContainerStyled } from '../styles/style'
import { SelectChangeEvent } from '@mui/material/Select'
import { Box, styled } from '@mui/material'
import { RefillDialogProps } from 'types'
import Select from './select'
import NumberInput from './numberInput'

const ContainerStyled = styled(Box)(() => ({
    display: 'flex',
    flexFlow: 'column',
    justifyContent: 'space-between',
    padding: remCalc(8),
    gap: remCalc(20)
}))

export default ({ open, onRefill, onCancel, currencies }: RefillDialogProps) => {
    const [currencyId, setCurrencyId] = useState('' + (currencies ? currencies[0].id : 0))
    const [value, setValue] = useState<number | undefined>(undefined)
    const [error, setError] = useState(false)

    const handleRefill = useCallback(() => {
        if (!value || error)
            return
        onRefill(Number(currencyId), value)
    }, [value, error, currencyId])

    const handleSelector = useCallback((event: SelectChangeEvent<unknown>) => {
        setCurrencyId(event.target.value as string)
    }, [])

    return <Dialog
        open={open}
    >
        <DialogContent>
            <ContainerStyled>
                <Select
                    items={currencies ? currencies : []}
                    value={currencyId}
                    variant="medium"
                    onChange={handleSelector}
                />
                <NumberInput value={value} label={'Amount'} onChange={setValue} onError={setError}/>
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
