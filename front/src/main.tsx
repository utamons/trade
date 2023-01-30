import React from 'react'
import { Box, Grid, styled } from '@mui/material'
import Dashboard from './components/dashboard/dashboard'
import Work from './components/work'
import { remCalc } from './utils/utils'


const MainStyled = styled(Box)(({ theme }) => ({
    display: 'flex',
    color: 'black',
    fontSize: 14,
    backgroundColor: theme.palette.background.default,
    fontWeight: 'normal',
    fontFamily: 'sans',
    width: remCalc(1920),
    flexDirection: 'column',
    minHeight: '100vh'
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
