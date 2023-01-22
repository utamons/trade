import { Box, styled } from '@mui/material'
import { Loadable, remCalc } from '../utils/utils'
import { useContext } from 'react'
import { TradeContext } from '../utils/trade-context'
import DateTime from './dateTime'

const ContainerStyled = styled(Box)(({theme}) => ({
    display: 'flex',
    flexFlow: 'row',
    alignItems: 'center',
    color: theme.palette.primary.main,
    backgroundColor: theme.palette.background.paper,
    fontSize: remCalc(18),
    fontWeight: 'normal',
    justifyContent: 'flex-start',
    width: '100%'
}))

export default () => {
    const { all } = useContext(TradeContext)
    const { isLoading, brokers, currencies, tickers, markets } = all

    return <ContainerStyled>
        <DateTime />
        <Loadable isLoading={isLoading}>
            <Box>Broker: {brokers?brokers[0].name:''}</Box>
        </Loadable>
    </ContainerStyled>
    /*
        Deposit:
             KZT: ???
             USD: ???
        Outcome: ???
        Profit: % to capital
        Capital: ???
        [ REFILL ] [ EXCHANGE ]*/
}
