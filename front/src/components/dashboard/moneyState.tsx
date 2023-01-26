// noinspection TypeScriptValidateTypes

import React from 'react'
import { Box, styled } from '@mui/material'
import { remCalc } from '../../utils/utils'
import { MoneyStateType } from 'types'

const ContainerStyled = styled(Box)(({ theme }) => ({
    borderRight: `solid ${remCalc(1)}`,
    borderColor: theme.palette.text.primary,
    padding: remCalc(10),
   // paddingRight: remCalc(10),
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

export default ({ capital, profit }: MoneyStateType) => {
    return <ContainerStyled>
            <BoxStyled>
                <div>Capital:</div> <div>${capital}</div>
            </BoxStyled>
            <BoxStyled>
                <div>Profit:</div> <div>{profit}%</div>
            </BoxStyled>
    </ContainerStyled>
}
