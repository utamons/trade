// noinspection TypeScriptValidateTypes

import React, { useCallback, useState } from 'react'
import { Box, styled } from '@mui/material'
import { remCalc } from '../../utils/utils'
import { BrokerProps } from 'types'
import MenuItem from '@mui/material/MenuItem'
import FormControl from '@mui/material/FormControl'
import Select, { SelectChangeEvent } from '@mui/material/Select'
import Button from '../button'
import Refill from '../refill'
import { ButtonContainerStyled, SelectorContainerStyled } from '../../styles/style'

const ContainerStyled = styled(Box)(({ theme }) => ({
    borderRight: `solid ${remCalc(1)}`,
    borderColor: theme.palette.text.primary,
    padding: remCalc(10),
    fontSize: remCalc(18),
    fontWeight: 'normal',
    display: 'flex',
    flexFlow: 'column',
    justifyContent: 'space-between'
}))


export default ({ brokers, currencies, currentBroker, setCurrentBrokerId, refill }: BrokerProps) => {
    const [id, setId] = useState('' + (currentBroker ? currentBroker.id : 1))
    const [refillOpen, setRefillOpen] = useState(false)

    const handleChange = useCallback((event: SelectChangeEvent) => {
        setId(event.target.value as string)
        setCurrentBrokerId(Number(event.target.value))
    }, [])

    const openRefillDialog = useCallback(() => {
        setRefillOpen(true)
    }, [])

    const commitRefill = useCallback((currencyId: number, amount: number) => {
        refill(currencyId, amount)
        setRefillOpen(false)
    }, [])

    const openExchangeDialog = useCallback(() => {
        console.log('Exchange')
    }, [])

    const cancelRefill = useCallback(()=>{
        setRefillOpen(false)
    }, [])

    return <ContainerStyled>
        <SelectorContainerStyled>
            <FormControl variant="standard" sx={{ m: 1, minWidth: 120 }}>
                <Select
                    labelId="demo-simple-select-standard-label"
                    id="demo-simple-select-standard"
                    value={id}
                    onChange={handleChange}
                >
                    {
                        brokers ?
                            brokers.map(broker =>
                                <MenuItem key={broker.id} value={broker.id}>{broker.name}</MenuItem>) :
                            <></>
                    }
                </Select>
            </FormControl>
        </SelectorContainerStyled>
        <ButtonContainerStyled>
            <Button style={{ minWidth: remCalc(101) }} text="Refill" onClick={openRefillDialog}/>
            <Button style={{ minWidth: remCalc(101) }} text="Exchange" onClick={openExchangeDialog}/>
        </ButtonContainerStyled>
        <Refill open={refillOpen} onRefill={commitRefill} onCancel={cancelRefill} currencies={currencies} />
    </ContainerStyled>

}
