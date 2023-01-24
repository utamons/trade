import React, { useContext } from 'react'
import { Box, Container, Grid, styled } from '@mui/material'
import { TradeContext } from './trade-context'
import Dashboard from './components/dashboard/dashboard'
import Work from './components/work'


const MainStyled = styled(Box)(() => ({
    alignItems: 'center',
    display: 'flex',
    color: 'black',
    fontSize: 18,
    fontWeight: 'normal',
    justifyContent: 'center',
    width: '100%'
}))
export default () => {

    return (
        <MainStyled>
                <Grid id="mainContainer" container spacing={0}>
                    <Grid item xs={12}>
                        <Dashboard />
                    </Grid>
                    <Grid item xs={12}>
                        <Work />
                    </Grid>
                </Grid>
        </MainStyled>
    )
}
