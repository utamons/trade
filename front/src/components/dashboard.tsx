import { Box, styled } from '@mui/material'
import { Loadable, remCalc } from '../utils/utils'
import { useContext } from 'react'
import { TradeContext } from '../utils/trade-context'
import DateTime from './dateTime'

const ContainerStyled = styled(Box)(({theme}) => ({
    border: `solid ${remCalc(1)}`,
    borderColor: theme.palette.text.primary,
    display: 'flex',
    flexFlow: 'row',
    alignItems: 'center',
    color: theme.palette.text.primary,
    backgroundColor: theme.palette.background.default,
    fontSize: remCalc(18),
    fontWeight: 'normal',
    justifyContent: 'flex-start',
    width: '100%'
}))

export default () => {
    const { all } = useContext(TradeContext)
    if (!all)
        return <></>
    const { isLoading, brokers, currencies, tickers, markets } = all

    return <ContainerStyled>
        <DateTime />
        <Loadable isLoading={isLoading}>
            <Box>Broker: {brokers?brokers[0].name:''}</Box>
        </Loadable>
    </ContainerStyled>
}
