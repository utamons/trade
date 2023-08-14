import React, { useContext } from 'react'
import { Box, styled } from '@mui/material'
import { MainMenuItem } from './mainMenuItem'
import { remCalc } from '../../utils/utils'
import { TradeContext } from '../../trade-context'

const Container = styled(Box)(({ theme }) => ({
    display: 'flex',
    fontSize: remCalc(14),
    backgroundColor: theme.palette.background.default,
    fontWeight: 'normal',
    fontFamily: 'sans-serif',
    flexDirection: 'row',
    alignItems: 'center',
    borderBottom: `solid ${remCalc(1)}`,
    borderColor: theme.palette.text.disabled,
    paddingLeft: remCalc(15)
}))

export const MainMenu = () => {

    const { brokers,
        currentBroker,
        setRefillDialogVisible,
        setCorrectionDialogVisible,
        setExchangeDialogVisible,
        setCurrentBrokerId,
        setOpenDialogVisible } = useContext(TradeContext)

    const brokerOptions = []

    if (brokers) {
        for (const broker of brokers) {
            brokerOptions.push({
                checked: broker.id === currentBroker?.id,
                name: broker.name,
                onClick: () => setCurrentBrokerId(broker.id)
            })
        }
    }

    const viewOptions = [
        { name: 'Trade log', onClick: () => console.log('View 1') },
        { name: 'Stats', onClick: () => console.log('View 2') }
    ]

    const actionsOptions = [
        { name: 'Open', onClick: () => setOpenDialogVisible(true) },
        { name: 'Refill', onClick: () => setRefillDialogVisible(true) },
        { name: 'Correction', onClick: () => setCorrectionDialogVisible(true) },
        { name: 'Exchange', onClick: () => setExchangeDialogVisible(true) }
    ]

    return (
        <Container>
            <MainMenuItem name="Broker" options={brokerOptions} />
            <MainMenuItem name="Actions" options={actionsOptions} />
            <MainMenuItem name="View" options={viewOptions} />
        </Container>
    )
}
