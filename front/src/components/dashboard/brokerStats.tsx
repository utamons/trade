import React, { useContext } from 'react'
import { Box, styled } from '@mui/material'
import { GREEN, profitColor, remCalc } from '../../utils/utils'
import { useTheme } from '@emotion/react'
import { TradeContext } from '../../trade-context'

const BoxStyled = styled(Box)(() => ({
    display: 'flex',
    justifyContent: 'space-between',
    gap: remCalc(5),
    paddingRight: remCalc(20)
}))

export default () => {
    const { brokerStats, currentBroker } = useContext(TradeContext)
    const { tradeAccounts, riskBase, open, outcome } = brokerStats

    const theme = useTheme()
    // noinspection TypeScriptUnresolvedVariable
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    const defaultColor = theme.palette.text.primary

    return <>
        <BoxStyled>
            <div>Broker:</div> <Box sx={{ color: GREEN, fontWeight: 'bolder' }}>{currentBroker?.name}</Box>
        </BoxStyled>
        {tradeAccounts.map(acc => <BoxStyled key={acc.id}>
            <div>{acc.currency.name}:</div> <div>{acc.amount}</div>
        </BoxStyled>)}
        <BoxStyled>
            <div>Outcome:</div> <Box sx={profitColor(outcome, defaultColor)}>{outcome}$</Box>
        </BoxStyled>
        <BoxStyled>
            <div>Open:</div> <div>{open}</div>
        </BoxStyled>
        <BoxStyled>
            <div>Risk base:</div> <div>$ {riskBase}</div>
        </BoxStyled>
    </>

}
