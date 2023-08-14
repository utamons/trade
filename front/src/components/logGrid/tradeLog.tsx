import React, { useContext } from 'react'
import { Box, styled } from '@mui/material'
import { remCalc } from '../../utils/utils'
import LogGrid from './logGrid'
import { TradeContext } from '../../trade-context'
import Paginator from './paginator'
import CircularProgress from '@mui/material/CircularProgress'

const ContainerStyled = styled(Box)(({ theme }) => ({
    alignItems: 'top',
    flexFlow: 'column',
    display: 'flex',
    color: theme.palette.text.primary,
    backgroundColor: theme.palette.background.default,
    fontWeight: 'normal',
    justifyContent: 'left',
    padding: remCalc(20),
    gap: remCalc(3)
}))

const RowStyled = styled(Box)(() => ({
    alignItems: 'left',
    display: 'flex',
    justifyContent: 'left',
    width: '100%'
}))

const TradeLog = () => {
    const { isLoading } = useContext(TradeContext)

    return isLoading ? <CircularProgress size={20}/> : <ContainerStyled>
        <RowStyled>
            <LogGrid />
        </RowStyled>
        <RowStyled>
            <Paginator/>
        </RowStyled>
    </ContainerStyled>
}

export default TradeLog
