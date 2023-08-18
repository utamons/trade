import React, { useCallback, useContext } from 'react'
import { Box, Grid, styled } from '@mui/material'
import Dashboard from './components/dashboard/dashboard'
import TradeLog from './components/logGrid/tradeLog'
import { remCalc } from './utils/utils'
import { MainMenu } from './components/menu/mainMenu'
import { LOG_VIEW, STATS_VIEW, TradeContext } from './trade-context'
import Open from './components/dialogs/openDialog'
import Refill from './components/dialogs/refill'
import Exchange from './components/dialogs/exchangeDialog'
import Stats from './components/stats/stats'


const MainStyled = styled(Box)(({ theme }) => ({
    display: 'flex',
    color: 'black',
    fontSize: 14,
    backgroundColor: theme.palette.background.default,
    fontWeight: 'normal',
    fontFamily: 'sans-serif',
    width: remCalc(1920),
    flexDirection: 'column',
    minHeight: '100vh'
}))

const LogView = () => {
    const { currentView } = useContext(TradeContext)
    return currentView == LOG_VIEW ? <>
        <Grid item xs={12}>
            <Dashboard/>
        </Grid>
        <Grid item xs={12}>
            <TradeLog/>
        </Grid> </> : <></>
}

const StatsView = () => {
    const { currentView } = useContext(TradeContext)
    return currentView == STATS_VIEW ? <>
        <Grid item xs={12}>
            <Stats />
        </Grid> </> : <></>
}

export default () => {
    const {
        currentBroker,
        isLoading,
        openDialogVisible,
        setOpenDialogVisible,
        setRefillDialogVisible,
        setCorrectionDialogVisible,
        setExchangeDialogVisible,
        refillDialogVisible,
        correctionDialogVisible,
        exchangeDialogVisible,
        currencies,
        refill,
        correction,
        refreshDashboard
    } = useContext(TradeContext)

    const handleCloseOpenDialog = useCallback(() => {
        setOpenDialogVisible(false)
    }, [])

    const commitRefill = useCallback((currencyId: number, amount: number) => {
        refill(currentBroker?.id ?? 0, currencyId, amount)
        setRefillDialogVisible(false)
    }, [currentBroker])

    const commitCorrection = useCallback((currencyId: number, amount: number) => {
        correction(currentBroker?.id ?? 0, currencyId, amount)
        setCorrectionDialogVisible(false)
    }, [currentBroker])

    const cancelRefill = useCallback(() => {
        setRefillDialogVisible(false)
    }, [])

    const cancelCorrection = useCallback(() => {
        setCorrectionDialogVisible(false)
    }, [])

    const exchangeClose = useCallback(() => {
        setExchangeDialogVisible(false)
        refreshDashboard()
    }, [])


    return (
        <MainStyled>
            <Grid id="mainContainer" container spacing={0}>
                <Grid item xs={12}>
                    <MainMenu/>
                </Grid>
                <LogView/>
                <StatsView/>
            </Grid>
            {isLoading ? <></> : <>
                <Open isOpen={openDialogVisible} onClose={handleCloseOpenDialog}/>
                <Refill open={refillDialogVisible} negativeAllowed={false} title="Refill" onSubmit={commitRefill}
                        onCancel={cancelRefill}
                        currencies={currencies}/>
                <Refill open={correctionDialogVisible} negativeAllowed={true} title="Correction"
                        onSubmit={commitCorrection}
                        onCancel={cancelCorrection} currencies={currencies}/>
                <Exchange open={exchangeDialogVisible} onClose={exchangeClose} currencies={currencies}/></>}
        </MainStyled>
    )
}
