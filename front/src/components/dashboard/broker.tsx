// noinspection TypeScriptValidateTypes

import React, { useState } from 'react'
import { Box, styled } from '@mui/material'
import { remCalc } from '../../utils/utils'
import { BrokerProps } from 'types'
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import Select, { SelectChangeEvent } from '@mui/material/Select';
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
    const [id, setId] = useState('' + (currentBroker ? currentBroker.id : 1));
    const [refillOpen, setRefillOpen] = useState(false)

    const handleChange = (event: SelectChangeEvent) => {
        setId(event.target.value as string);
        setCurrentBrokerId(event.target.value)
    };

    const handleRefill = () => {
        setRefillOpen(true)
    }

    const refillClose = (currencyId: number, amount: number) => {
        refill(currencyId,amount)
        setRefillOpen(false)
    }

    const handleExchange = () => {
        console.log('Exchange')
    }

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
                        brokers ? brokers.map(broker => <MenuItem key={broker.id} value={broker.id}>{broker.name}</MenuItem>) : <></>
                    }
                </Select>
            </FormControl>
        </SelectorContainerStyled>
        <ButtonContainerStyled>
            <Button style={{ minWidth: remCalc(101) }} text="Refill" disabled={false} onClick={handleRefill}/>
            <Button style={{ minWidth: remCalc(101) }} text="Exchange" disabled={false} onClick={handleExchange}/>
        </ButtonContainerStyled>
        <Refill open={refillOpen} onClose={refillClose} currencies={currencies} />
    </ContainerStyled>

}
