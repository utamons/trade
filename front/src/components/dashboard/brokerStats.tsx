// noinspection TypeScriptValidateTypes

import React from 'react'
import { Box, Grid, styled } from '@mui/material'
import { remCalc } from '../../utils/utils'
import { BrokerProps, BrokerStatsType } from 'types'
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import Select, { SelectChangeEvent } from '@mui/material/Select';
import Button from '../button'

const ContainerStyled = styled(Box)(({ theme }) => ({
    borderRight: `solid ${remCalc(1)}`,
    borderColor: theme.palette.text.primary,
    padding: remCalc(10),
   // paddingRight: remCalc(10),
    fontSize: remCalc(14),
    height: remCalc(131),
    display: 'flex',
    flexFlow: 'column wrap',
    gap: remCalc(20)
}))

const BoxStyled = styled(Box)(() => ({
    width: remCalc(160),
    display: 'flex',
    justifyContent: 'space-between'
}))

export default ({ accounts, open, outcome, avgOutcome, avgProfit }: BrokerStatsType) => {
    const width = accounts.length>0?remCalc(337):remCalc(164)
    return <ContainerStyled sx={{width}}>
            {accounts.map(acc => <BoxStyled>
                <div>{acc.currency.name}:</div> <div>{acc.amount}</div>
            </BoxStyled>)}
            <BoxStyled>
                <div>Outcome:</div> <div>${outcome}</div>
            </BoxStyled>
            <BoxStyled>
                <div>Avg. outcome:</div> <div>${avgOutcome}</div>
            </BoxStyled>
            <BoxStyled>
                <div>Avg. profit:</div> <div>{avgProfit}%</div>
            </BoxStyled>
            <BoxStyled>
                <div>Open:</div> <div>{open}</div>
            </BoxStyled>
    </ContainerStyled>

}
