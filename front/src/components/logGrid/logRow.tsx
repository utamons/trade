import { Box, Grid, IconButton, styled } from '@mui/material'
import { dateTimeWithOffset, money, remCalc } from '../../utils/utils'
import CloseIcon from '@mui/icons-material/Close'
import { TradeLog } from 'types'
import React, { useCallback, useState } from 'react'
import ExpandButton from './expandButton'

export const CloseButtonStyled = styled(IconButton)(({ theme }) => ({
    width: remCalc(25),
    height: remCalc(25),
    marginTop: remCalc(2),
    color: theme.palette.text.primary
}))


const RowBox = styled(Grid)(({ theme }) => ({
    borderBottom: `solid ${remCalc(1)}`,
    borderColor: theme.palette.divider,
    paddingTop: remCalc(7),
    paddingBottom: remCalc(7),
    width: '100%'
}))

const Item = styled(Grid)(({ theme }) => ({
    alignItems: 'center',
    display: 'flex',
    color: theme.palette.text.primary,
    justifyContent: 'flex-start',
    padding: `${remCalc(7)} 0 ${remCalc(7)} ${remCalc(10)}`
}))

const FieldBox = styled(Box)(({ theme }) => ({
    alignItems: 'center',
    display: 'flex',
    color: theme.palette.text.primary,
    justifyContent: 'flex-start',
    margin: remCalc(2),
    padding: `${remCalc(7)} 0 ${remCalc(7)} ${remCalc(10)}`
}))

const CloseButton = () => <CloseButtonStyled>
    <CloseIcon fontSize="small"/>
</CloseButtonStyled>

interface LogRowProps {
    key: number,
    logItem: TradeLog
}

interface ExpandableProps {
    key: number,
    logItem: TradeLog,
    expandHandler: (expanded: boolean) => void
}

const Expanded = ({ key, logItem, expandHandler }: ExpandableProps) => {
    return <RowBox key={key} container>

    </RowBox>
}

const Collapsed = ({ key, logItem, expandHandler }: ExpandableProps) => {
    const { broker, market, ticker, position, dateOpen, dateClose, currency,
    priceClose, priceOpen, itemNumber, outcomePercent, fees, volume, outcome } = logItem

    const tz = market.timezone

    return <RowBox key={key} container columns={53}>
        <Item item xs={4}>{broker.name}</Item>
        <Item item xs={3}>{market.name}</Item>
        <Item item xs={3}>{ticker.shortName}</Item>
        <Item item xs={2}>{position}</Item>
        <Item item xs={6}>{dateTimeWithOffset(dateOpen, tz)}</Item>
        <Item item xs={6}>{dateClose ? dateTimeWithOffset(dateClose, tz) : '-'}</Item>
        <Item item xs={4}>{money(currency.name, priceOpen)}</Item>
        <Item item xs={4}>{priceClose ? money(currency.name, priceClose) : '-'}</Item>
        <Item item xs={3}>{itemNumber}</Item>
        <Item item xs={4}>{money(currency.name, volume)}</Item>
        <Item item xs={4}>{money(currency.name, outcome)}</Item>
        <Item item xs={4}>{outcomePercent != undefined ? `${outcomePercent} %` : '-'}</Item>
        <Item item xs={4}>{money('USD', fees)}</Item>
        <Item item xs={2}>
            <ExpandButton onClick={expandHandler}/>
            {dateClose ? <></> : <CloseButton/>}
        </Item>
    </RowBox>
}

export const LogRow = ({ key, logItem }: LogRowProps) => {
    const [expanded, setExpanded] = useState(false)
    const expandHandler = useCallback((expanded: boolean) => {
        setExpanded(expanded)
    }, [])

    return <Collapsed key={key} logItem={logItem} expandHandler={expandHandler}/>
}
