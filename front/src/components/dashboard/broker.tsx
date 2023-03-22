import React, { Dispatch, useCallback, useEffect, useState } from 'react'
import { Box, styled } from '@mui/material'
import { remCalc } from '../../utils/utils'
import { BrokerProps, FormAction, FormActionPayload, FormState } from 'types'
import Button from '../tools/button'
import Refill from '../dialogs/refill'
import { ButtonContainerStyled } from '../../styles/style'
import Exchange from '../dialogs/exchange'
import Select from '../tools/select'
import { getFieldValue, useForm } from '../dialogs/dialogUtils'

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

const SelectorContainerStyled = styled(Box)(() => ({
    padding: remCalc(20),
    display: 'flex',
    flexFlow: 'row',
    alignItems: 'center',
    fontWeight: 'normal',
    justifyContent: 'center'
}))

const initFormState = (formState: FormState, dispatch: Dispatch<FormAction>, brokerId: number) => {
    if (formState.isInitialized)
        return

    const payload: FormActionPayload = {
        valuesNumeric: [
            {
                name: 'brokerId',
                valid: true,
                value: brokerId
            }
        ]
    }

    dispatch({ type: 'init', payload })
}

export default ({ brokers, currencies, currentBroker, setCurrentBrokerId, refill, correction, exchange }: BrokerProps) => {
    const { formState, dispatch } = useForm()
    const [refillOpen, setRefillOpen] = useState(false)
    const [correctionOpen, setCorrectionOpen] = useState(false)
    const [exchangeOpen, setExchangeOpen] = useState(false)

    initFormState(formState, dispatch, currentBroker ? currentBroker.id : 0)

    const id = '' + getFieldValue('brokerId', formState)

    useEffect(() => {
        setCurrentBrokerId(Number(id))
    }, [id])

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
                name="brokerId"
                items={brokers ? brokers : []}
                dispatch={dispatch}
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
