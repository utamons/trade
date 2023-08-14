// noinspection TypeScriptValidateTypes

import React, { useContext } from 'react'
import { Box, styled } from '@mui/material'
import { profitColor, remCalc } from '../../utils/utils'
import { useTheme } from '@emotion/react'
import { TradeContext } from '../../trade-context'

const BoxStyled = styled(Box)(() => ({
    display: 'flex',
    justifyContent: 'space-between',
    gap: remCalc(5),
    paddingRight: remCalc(20)
}))

export default () => {
    const { moneyState } = useContext(TradeContext)
    const { capital, profit } = moneyState
    const theme = useTheme()
    // noinspection TypeScriptUnresolvedVariable
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    const defaultColor = theme.palette.text.primary

    return <>
        <BoxStyled>
            <div>Capital:</div> <div>$ {capital}</div>
        </BoxStyled>
        <BoxStyled>
            <div>Profit:</div> <Box sx={profitColor(profit, defaultColor)}>{profit}%</Box>
        </BoxStyled>
    </>
}
