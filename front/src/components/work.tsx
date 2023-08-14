import React, { useCallback, useContext } from 'react'
import { Box, styled } from '@mui/material'
import { remCalc } from '../utils/utils'
import LogGrid from './logGrid/logGrid'
import { TradeContext } from '../trade-context'
import Paginator from './logGrid/paginator'
import CircularProgress from '@mui/material/CircularProgress'
import Open from './logGrid/dialogs/openDialog'
import Refill from './dialogs/refill'
import Exchange from './dialogs/exchange'

const ContainerStyled = styled(Box)(({ theme }) => ({
    alignItems: 'top',
    flexFlow: 'column',
    display: 'flex',
    color: theme.palette.text.primary,
    backgroundColor: theme.palette.background.default,
    fontWeight: 'normal',
    justifyContent: 'left',
    padding: remCalc(20),
    gap: remCalc(3)
}))

const RowStyled = styled(Box)(() => ({
    alignItems: 'left',
    display: 'flex',
    justifyContent: 'left',
    width: '100%'
}))

const WorkInt = () => {
    const {
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
        refill(currencyId, amount)
        setRefillDialogVisible(false)
    }, [])

    const commitCorrection = useCallback((currencyId: number, amount: number) => {
        correction(currencyId, amount)
        setCorrectionDialogVisible(false)
    }, [])

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


    return <ContainerStyled>
        <RowStyled>
            <LogGrid />
        </RowStyled>
        <RowStyled>
            <Paginator/>
        </RowStyled>
        <Open
            isOpen={openDialogVisible}
            onClose={handleCloseOpenDialog}/>
        <Refill open={refillDialogVisible} negativeAllowed={false} title="Refill" onSubmit={commitRefill} onCancel={cancelRefill}
                currencies={currencies}/>
        <Refill open={correctionDialogVisible} negativeAllowed={true} title="Correction" onSubmit={commitCorrection}
                onCancel={cancelCorrection} currencies={currencies}/>
        <Exchange open={exchangeDialogVisible} onClose={exchangeClose} currencies={currencies}/>
    </ContainerStyled>
}

export default () => {
    const { isLoading } = useContext(TradeContext)
    if (isLoading)
        return <CircularProgress size={20}/>

    return <WorkInt />
}
