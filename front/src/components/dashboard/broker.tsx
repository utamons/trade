import React, { useCallback, useState } from 'react'
import { Box, styled } from '@mui/material'
import { remCalc } from '../../utils/utils'
import { BrokerProps } from 'types'
import { SelectChangeEvent } from '@mui/material/Select'
import Button from '../button'
import Refill from '../refill'
import { ButtonContainerStyled, SelectorContainerStyled } from '../../styles/style'
import Exchange from '../exchange'
import Select from '../select'

const ContainerStyled = styled(Box)(({ theme }) => ({
    borderRight: `solid ${remCalc(1)}`,
    borderColor: theme.palette.text.primary,
    padding: remCalc(10),
    fontSize: remCalc(18),
    fontWeight: 'normal',
    display: 'flex',
    flexFlow: 'column',
    justifyContent: 'space-between'
}))


export default ({ brokers, currencies, currentBroker, setCurrentBrokerId, refill, correction, exchange }: BrokerProps) => {
    const [id, setId] = useState('' + (currentBroker ? currentBroker.id : 1))
    const [refillOpen, setRefillOpen] = useState(false)
    const [correctionOpen, setCorrectionOpen] = useState(false)
    const [exchangeOpen, setExchangeOpen] = useState(false)

    const handleChange = useCallback((event: SelectChangeEvent<unknown>) => {
        setId(event.target.value as string)
        setCurrentBrokerId(Number(event.target.value))
    }, [])

    const openRefillDialog = useCallback(() => {
        setRefillOpen(true)
    }, [])

    const openCorrectionDialog = useCallback(() => {
        setCorrectionOpen(true)
    }, [])

    const commitRefill = useCallback((currencyId: number, amount: number) => {
        refill(currencyId, amount)
        setRefillOpen(false)
    }, [])

    const commitCorrection = useCallback((currencyId: number, amount: number) => {
        correction(currencyId, amount)
        setCorrectionOpen(false)
    }, [])

    const commitExchange = useCallback((
        currencyFromId: number,
        amountFrom: number,
        currencyToId: number,
        amountTo: number) => {
        exchange(currencyFromId, amountFrom, currencyToId, amountTo)
        setExchangeOpen(false)
    }, [])

    const openExchangeDialog = useCallback(() => {
        setExchangeOpen(true)
    }, [])

    const cancelRefill = useCallback(() => {
        setRefillOpen(false)
    }, [])

    const cancelCorrection = useCallback(() => {
        setCorrectionOpen(false)
    }, [])

    const cancelExchange = useCallback(() => {
        setExchangeOpen(false)
    }, [])

    return <ContainerStyled>
        <SelectorContainerStyled>
            <Select
                value={id}
                items={brokers ? brokers : []}
                onChange={handleChange}
            />
        </SelectorContainerStyled>
        <ButtonContainerStyled>
            <Button style={{ minWidth: remCalc(101) }} text="Refill" onClick={openRefillDialog}/>
            <Button style={{ minWidth: remCalc(101) }} text="Correction" onClick={openCorrectionDialog}/>
            <Button style={{ minWidth: remCalc(101) }} text="Exchange" onClick={openExchangeDialog}/>
        </ButtonContainerStyled>
        <Refill open={refillOpen} negativeAllowed={false} title="Refill" onSubmit={commitRefill} onCancel={cancelRefill} currencies={currencies}/>
        <Refill open={correctionOpen} negativeAllowed={true} title="Correction" onSubmit={commitCorrection} onCancel={cancelCorrection} currencies={currencies}/>
        <Exchange open={exchangeOpen} onExchange={commitExchange} onCancel={cancelExchange} currencies={currencies}/>
    </ContainerStyled>

}
