import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import DialogTitle from '@mui/material/DialogTitle'
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

const DialogTitleStyled = styled(DialogTitle)(() => ({
    fontSize: remCalc(16),
    fontWeight: 501
}))

export default ({ open, title, onSubmit, onCancel, currencies }: RefillDialogProps) => {
    const [currencyId, setCurrencyId] = useState('' + (currencies ? currencies[0].id : 0))
    const [value, setValue] = useState<number | undefined>(undefined)
    const [error, setError] = useState(false)

    const handleSubmit = useCallback(() => {
        if (!value || error)
            return
        onSubmit(Number(currencyId), value)
    }, [value, error, currencyId])

    const handleSelector = useCallback((event: SelectChangeEvent<unknown>) => {
        setCurrencyId(event.target.value as string)
    }, [])

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
                    onChange={handleSelector}
                />
                <NumberInput value={value} label={'Amount'} onChange={setValue} onError={setError}/>
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
