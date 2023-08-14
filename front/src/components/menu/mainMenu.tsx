import React from 'react'
import { Box, styled } from '@mui/material'
import { MainMenuItem } from './mainMenuItem'
import { remCalc } from '../../utils/utils'

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

    const brokerOptions = [
        { name: 'Interactive', onClick: () => console.log('Broker 1') },
        { name: 'FreedomFN', onClick: () => console.log('Broker 2') }
    ]

    const viewOptions = [
        { name: 'Trade log', onClick: () => console.log('View 1') },
        { name: 'Stats', onClick: () => console.log('View 2') }
    ]

    const actionsOptions = [
        { name: 'Open', onClick: () => console.log('Action 1') },
        { name: 'Refill', onClick: () => console.log('Action 2') },
        { name: 'Correction', onClick: () => console.log('Action 3') },
        { name: 'Exchange', onClick: () => console.log('Action 4') }
    ]

    return (
        <Container>
            <MainMenuItem name="Broker" options={brokerOptions} />
            <MainMenuItem name="Actions" options={actionsOptions} />
            <MainMenuItem name="View" options={viewOptions} />
        </Container>
    )
}
