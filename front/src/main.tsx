import React, { useContext } from 'react'
import { Box, Container, Grid, styled } from '@mui/material'
import { TradeContext } from './utils/trade-context'


const MainStyled = styled(Box)(() => ({
    alignItems: 'center',
    display: 'flex',
    color: 'black',
    fontSize: 22,
    fontWeight: 'bold',
    justifyContent: 'center',
    width: '100%'

}))
export default () => {

    const { all } = useContext(TradeContext)
    const { isLoading, brokers, currencies, tickers, markets } = all

    return (
        <MainStyled>
            <Container>
                <Grid id="mainContainer" container spacing={0} className="content">
                    Test: {isLoading ? 'Loading' : `${brokers[0].name} ${currencies[0].name} ${tickers[0].name} ${markets[0].name}`}
                </Grid>
            </Container>
        </MainStyled>
    )
}
