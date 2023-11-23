import React, { Dispatch } from 'react'
import Dialog from '@mui/material/Dialog'
import DialogContent from '@mui/material/DialogContent'
import { getFieldErrorText, getFieldValue, isFieldValid, useForm } from './dialogs/dialogUtils'
import DialogActions from '@mui/material/DialogActions'
import { ButtonContainerStyled } from '../styles/style'
import Button from './tools/button'
import { remCalc } from '../utils/utils'
import { Box, Grid, styled } from '@mui/material'
import { FormAction, FormActionPayload, FormState } from 'types'
import { MAX_DEPOSIT_PC, MAX_RISK_PC, MAX_RISK_REWARD_PC } from '../utils/constants'
import { positions } from './dialogs/openDialog'
import NumberFieldBox from './dialogs/components/numberFieldBox'

const ContainerStyled = styled(Box)(() => ({
    width: remCalc(300),
    display: 'flex',
    flexFlow: 'column',
    fontSize: remCalc(14),
    justifyContent: 'space-between',
    padding: remCalc(8),
    gap: remCalc(20)
}))

interface TradeCalculatorProps {
    open: boolean;
    onClose: () => void;
}

const initFormState = (formState: FormState, dispatch: Dispatch<FormAction>, positionId: number) => {
    if (formState.isInitialized)
        return

    const payload: FormActionPayload = {
        values: [
            {
                name: 'risk',
                valid: true,
                value: undefined
            },
            {
                name: 'riskRewardPc',
                valid: true,
                value: MAX_RISK_REWARD_PC
            },
            {
                name: 'depositPc',
                valid: true,
                value: MAX_DEPOSIT_PC
            },
            {
                name: 'positionId',
                valid: true,
                value: positionId
            },
            {
                name: 'limit',
                valid: true,
                value: undefined
            },
            {
                name: 'items',
                valid: true,
                value: undefined
            },
            {
                name: 'stopLoss',
                valid: true,
                value: undefined
            },
            {
                name: 'takeProfit',
                valid: true,
                value: undefined
            },
            {
                name: 'outcomeExp',
                valid: true,
                value: undefined
            },
            {
                name: 'riskPc',
                valid: true,
                value: MAX_RISK_PC
            },
            {
                name: 'breakEven',
                valid: true,
                value: undefined
            },
            {
                name: 'gainPc',
                valid: true,
                value: undefined
            },
            {
                name: 'stopPrice',
                valid: true,
                value: undefined
            },
            {
                name: 'atr',
                valid: true,
                value: undefined
            },
            {
                name: 'volume',
                valid: true,
                value: undefined
            },
            {
                name: 'fees',
                valid: true,
                value: undefined
            },
            {
                name: 'note',
                valid: true,
                value: undefined
            },
            {
                name: 'spread',
                valid: true,
                value: undefined
            }
        ]
    }

    dispatch({ type: 'init', payload })
}

export default ({ open, onClose }: TradeCalculatorProps) => {
    const { formState, dispatch } = useForm()

    initFormState(formState, dispatch, positions[0].id)

    const spread = getFieldValue('spread', formState) as number | undefined

    return <Dialog open={open}>
        <DialogContent>
            <ContainerStyled>
                <Grid container columns={1}>
                    <NumberFieldBox
                        label={'Spread:'}
                        value={spread}
                        valid={isFieldValid('spread', formState)}
                        errorText={getFieldErrorText('spread', formState)}
                        fieldName={'spread'}
                        dispatch={dispatch}/>
                </Grid>
            </ContainerStyled>
        </DialogContent>

        <DialogActions sx={{ justifyContent: 'center' }}>
            <ButtonContainerStyled>
                <Button style={{ minWidth: remCalc(101) }} text="Close" onClick={onClose}/>
            </ButtonContainerStyled>
        </DialogActions>
    </Dialog>
}
