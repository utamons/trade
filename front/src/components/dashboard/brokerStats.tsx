import React from 'react'
import { Box, styled } from '@mui/material'
import { profitColor, remCalc } from '../../utils/utils'
import { BrokerStatsType } from 'types'
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

export default ({ accounts, open, outcome, avgOutcome, avgProfit, borrowed }: BrokerStatsType) => {
    const width = accounts.length>0?remCalc(337):remCalc(164)

    const theme = useTheme()
    // noinspection TypeScriptUnresolvedVariable
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    const defaultColor = theme.palette.text.primary

    return <ContainerStyled sx={{ width }}>
        {accounts.map(acc => <BoxStyled key={acc.id}>
            <div>{acc.currency.name}:</div> <div>{acc.amount}</div>
        </BoxStyled>)}
        <BoxStyled>
            <div>Outcome:</div> <Box sx={profitColor(outcome, defaultColor)}>{outcome}$</Box>
        </BoxStyled>
        <BoxStyled>
            <div>Avg. outcome:</div> <Box sx={profitColor(avgOutcome, defaultColor)}>{avgOutcome}$</Box>
        </BoxStyled>
        <BoxStyled>
            <div>Avg. profit:</div> <Box sx={profitColor(avgProfit, defaultColor)}>{avgProfit}%</Box>
        </BoxStyled>
        {borrowed?<BoxStyled>
                <div>Borrowed:</div> <Box >${borrowed}</Box>
            </BoxStyled>:<></>}
        <BoxStyled>
            <div>Open:</div> <div>{open}</div>
        </BoxStyled>
    </ContainerStyled>

}
