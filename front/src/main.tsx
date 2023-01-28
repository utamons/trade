import React from 'react'
import { Box, Grid, styled } from '@mui/material'
import Dashboard from './components/dashboard/dashboard'
import Work from './components/work'


const MainStyled = styled(Box)(() => ({
    alignItems: 'center',
    display: 'flex',
    color: 'black',
    fontSize: 14,
    fontWeight: 'normal',
    fontFamily: 'sans',
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
