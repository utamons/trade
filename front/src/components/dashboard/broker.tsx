// noinspection TypeScriptValidateTypes

import React from 'react'
import { Box, styled } from '@mui/material'
import { remCalc } from '../../utils/utils'
import { BrokerProps } from 'types'
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import Select, { SelectChangeEvent } from '@mui/material/Select';
import Button from '../button'

const ContainerStyled = styled(Box)(({ theme }) => ({
    border: `solid ${remCalc(1)}`,
    borderRadius: remCalc(2),
    borderColor: theme.palette.text.primary,
    padding: remCalc(2),
    fontSize: remCalc(18),
    fontWeight: 'normal',
    display: 'flex',
    flexFlow: 'column',
    justifyContent: 'space-between'
}))

const SelectorContainerStyled = styled(Box)(() => ({
    padding: remCalc(20),
    display: 'flex',
    flexFlow: 'row',
    alignItems: 'center',
    fontSize: remCalc(18),
    fontWeight: 'normal',
    justifyContent: 'center'
}))

const ButtonContainerStyled = styled(Box)(() => ({
    padding: remCalc(8),
    display: 'flex',
    flexFlow: 'row',
    alignItems: 'center',
    fontSize: remCalc(18),
    fontWeight: 'normal',
    justifyContent: 'space-between',
    gap: remCalc(10)
}))

export default ({ brokers, currentBroker, setCurrentBrokerId }: BrokerProps) => {
    const [id, setId] = React.useState('' + (currentBroker ? currentBroker.id : 1));

    const handleChange = (event: SelectChangeEvent) => {
        setId(event.target.value as string);
        setCurrentBrokerId(event.target.value)
    };

    const handleRefill = () => {
        console.log('Refill')
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
    </ContainerStyled>

}
