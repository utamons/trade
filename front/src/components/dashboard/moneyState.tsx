// noinspection TypeScriptValidateTypes

import React from 'react'
import { Box, styled } from '@mui/material'
import { profitColor, remCalc } from '../../utils/utils'
import { MoneyStateType } from 'types'
import { useTheme } from '@emotion/react'

const ContainerStyled = styled(Box)(({ theme }) => ({
    borderRight: `solid ${remCalc(1)}`,
    borderColor: theme.palette.text.primary,
    padding: remCalc(10),
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

export default ({ capital, profit, riskBase }: MoneyStateType) => {
    const theme = useTheme()
    // noinspection TypeScriptUnresolvedVariable
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    const defaultColor = theme.palette.text.primary

    return <ContainerStyled>
        <BoxStyled>
            <div>Risk base:</div> <div>${riskBase}</div>
        </BoxStyled>
        <BoxStyled>
            <div>Capital:</div> <div>${capital}</div>
        </BoxStyled>
        <BoxStyled>
            <div>Profit:</div> <Box sx={profitColor(profit, defaultColor)}>{profit}%</Box>
        </BoxStyled>
    </ContainerStyled>
}
