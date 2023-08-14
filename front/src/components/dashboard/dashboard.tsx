import React, { useContext } from 'react'
import { Box, styled } from '@mui/material'
import { Loadable, remCalc } from '../../utils/utils'
import { TradeContext } from '../../trade-context'
import BrokerStats from './brokerStats'
import MoneyState from './moneyState'

const ContainerStyled = styled(Box)(({ theme }) => ({
    display: 'flex',
    flexFlow: 'row',
    alignItems: 'center',
    color: theme.palette.text.primary,
    backgroundColor: theme.palette.background.default,
    justifyContent: 'flex-start',

    borderColor: theme.palette.text.primary,
    padding: `${remCalc(15)} ${remCalc(20)} 0 ${remCalc(20)}`
}))

export default () => {
    const { isLoading } = useContext(TradeContext)

    return <Loadable isLoading={isLoading}>
        <ContainerStyled>
            <BrokerStats />
            <MoneyState />
        </ContainerStyled>
    </Loadable>
}
