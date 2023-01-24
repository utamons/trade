import { Box, styled } from '@mui/material'
import { Loadable, remCalc } from '../../utils/utils'
import { useContext } from 'react'
import { TradeContext } from '../../trade-context'
import Broker from './broker'

const ContainerStyled = styled(Box)(({theme}) => ({
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
    const { isLoading, brokers, currentBroker, setCurrentBrokerId } = all

    return <ContainerStyled>
        <Loadable isLoading={isLoading}>
            <Broker brokers={brokers} currentBroker={currentBroker} setCurrentBrokerId={setCurrentBrokerId} />
        </Loadable>
    </ContainerStyled>
}
