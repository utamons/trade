import { Box, Grid, styled } from '@mui/material'
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
    fontSize: remCalc(18),
    fontWeight: 'normal',
    justifyContent: 'flex-start',
    border: `solid ${remCalc(1)}`,
    borderRadius: remCalc(2),
    borderColor: theme.palette.text.primary
}))

export default () => {
    const { all } = useContext(TradeContext)
    if (!all)
        return <></>
    const { isLoading, brokers, currentBroker, brokerStats, moneyState, setCurrentBrokerId } = all

    return <Loadable isLoading={isLoading}>
        <ContainerStyled>
            <Broker brokers={brokers} currentBroker={currentBroker} setCurrentBrokerId={setCurrentBrokerId}/>
            <BrokerStats {...brokerStats}/>
            <MoneyState {...moneyState}/>
            <Markets />
        </ContainerStyled>
    </Loadable>
}
