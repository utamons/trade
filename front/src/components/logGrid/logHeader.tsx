import { Box, Grid, styled } from '@mui/material'
import React from 'react'
import { remCalc } from '../../utils/utils'

const HeaderBox = styled(Box)(({ theme }) => ({
    alignItems: 'center',
    display: 'flex',
    color: theme.palette.text.primary,
    backgroundColor: theme.palette.action.disabledBackground,
    justifyContent: 'center',
    margin: remCalc(2),
    paddingTop: remCalc(7),
    paddingBottom: remCalc(7)
}))

export const LogHeader = () => {
    return <>
        <Grid item xs={4}>
            <HeaderBox>Broker</HeaderBox>
        </Grid>
        <Grid item xs={3}>
            <HeaderBox>Market</HeaderBox>
        </Grid>
        <Grid item xs={3}>
            <HeaderBox>Ticker</HeaderBox>
        </Grid>
        <Grid item xs={2}>
            <HeaderBox>Position</HeaderBox>
        </Grid>
        <Grid item xs={6}>
            <HeaderBox>Open</HeaderBox>
        </Grid>
        <Grid item xs={6}>
            <HeaderBox>Closed</HeaderBox>
        </Grid>
        <Grid item xs={4}>
            <HeaderBox>Items bought</HeaderBox>
        </Grid>
        <Grid item xs={4}>
            <HeaderBox>Items sold</HeaderBox>
        </Grid>
        <Grid item xs={3}>
            <HeaderBox>Total bought</HeaderBox>
        </Grid>
        <Grid item xs={4}>
            <HeaderBox>Total sold</HeaderBox>
        </Grid>
        <Grid item xs={4}>
            <HeaderBox>Outcome</HeaderBox>
        </Grid>
        <Grid item xs={4}>
            <HeaderBox>Profit</HeaderBox>
        </Grid>
        <Grid item xs={3.5}>
            <HeaderBox>Fees</HeaderBox>
        </Grid>
        <Grid item xs={2.5}>
            <HeaderBox>&nbsp;</HeaderBox>
        </Grid></>
}
