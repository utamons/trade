import React, { useContext } from 'react'
import { Box, styled } from '@mui/material'
import { Loadable, remCalc } from '../../utils/utils'
import { TradeContext } from '../../trade-context'
import Broker from './broker'
import BrokerStats from './brokerStats'
import MoneyState from './moneyState'
import Markets from './markets'

const ContainerStyled = styled(Box)(({ theme }) => ({
    display: 'flex',
    flexFlow: 'row',
    alignItems: 'center',
    color: theme.palette.text.primary,
    backgroundColor: theme.palette.background.default,
    justifyContent: 'flex-start',
    border: `solid ${remCalc(1)}`,
    borderRadius: remCalc(2),
    borderColor: theme.palette.text.primary
}))

export default () => {
    const { isLoading } = useContext(TradeContext)

    return <Loadable isLoading={isLoading}>
        <ContainerStyled>
            <Broker />
            <BrokerStats />
            <MoneyState />
            <Markets/>
        </ContainerStyled>
    </Loadable>
}
