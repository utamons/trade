import React from 'react'
import { Box, styled } from '@mui/material'
import { Loadable, remCalc } from '../../utils/utils'
import { useContext } from 'react'
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
    const { all } = useContext(TradeContext)
    if (!all)
        return <></>
    const { isLoading, currencies, brokers, currentBroker, brokerStats, moneyState,
        setCurrentBrokerId, refill } = all

    return <Loadable isLoading={isLoading}>
        <ContainerStyled>
            <Broker
                brokers={brokers}
                currentBroker={currentBroker}
                currencies={currencies}
                refill={refill}
                setCurrentBrokerId={setCurrentBrokerId}/>
            {brokerStats? <BrokerStats {...brokerStats}/>:<></>}
            {moneyState? <MoneyState {...moneyState}/>: <></>}
            <Markets />
        </ContainerStyled>
    </Loadable>
}
