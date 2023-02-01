import React, { useCallback, useContext, useState } from 'react'
import { Box, styled } from '@mui/material'
import { remCalc } from '../utils/utils'
import Button from './button'
import { ButtonContainerStyled } from '../styles/style'
import LogGrid from './logGrid/logGrid'
import { TradeContext } from '../trade-context'
import Paginator from './logGrid/paginator'
import { ItemType, MarketType, PositionOpenType, TickerType, TradeLogPageType } from 'types'
import CircularProgress from '@mui/material/CircularProgress'
import Open from './logGrid/open'

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

interface WorkIntProps {
    logPage: TradeLogPageType,
    currentBroker: ItemType,
    markets: MarketType [],
    tickers: TickerType [],
    open: (open: PositionOpenType) => void
}

const WorkInt = (props: WorkIntProps) => {
    const [isOpen, setOpen] = useState(false)
    const [evaluate, setEvaluate] = useState(false)

    const { currentBroker, tickers, markets, open } = props

    const handleOpen = useCallback(() => {
        setEvaluate(false)
        setOpen(true)
    }, [])

    const handleCancel = useCallback(() => {
        setEvaluate(false)
        setOpen(false)
    }, [])

    const handleEvaluate = useCallback(() => {
        setEvaluate(true)
        setOpen(true)
    }, [])

    return <ContainerStyled>
        <RowStyled>
            <ButtonContainerStyled>
                <Button style={{ minWidth: remCalc(101) }} text="Open" onClick={handleOpen}/>
                <Button style={{ minWidth: remCalc(101) }} text="Evaluate" onClick={handleEvaluate}/>
            </ButtonContainerStyled>
        </RowStyled>
        <RowStyled>
            <LogGrid {...props.logPage}/>
        </RowStyled>
        <RowStyled>
            <Paginator/>
        </RowStyled>
        <Open
            isOpen={isOpen}
            evaluate={evaluate}
            open={open}
            tickers={tickers}
            markets={markets}
            currentBroker={currentBroker}
            onCancel={handleCancel}/>
    </ContainerStyled>
}

export default () => {
    const { all } = useContext(TradeContext)
    if (!all)
        return <CircularProgress size={20}/>
    const { currentBroker, markets, tickers, logPage, open } = all
    if (!logPage || !currentBroker || !markets || !tickers )
        return <CircularProgress size={20}/>

    return <WorkInt logPage={logPage} open={open} tickers={tickers} currentBroker={currentBroker} markets={markets}/>
}
