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

const Item = styled(Grid)(() => ({
    alignItems: 'center',
    display: 'flex',
    justifyContent: 'flex-start',
    padding: `${remCalc(7)} 0 ${remCalc(7)} ${remCalc(10)}`
}))

const FieldBox = styled(Box)(() => ({
    display: 'flex',
    justifyContent: 'space-between',
    width: remCalc(310),
    margin: `${remCalc(2)} ${remCalc(20)} ${remCalc(2)} ${remCalc(2)}`,
    padding: `${remCalc(7)} 0 ${remCalc(7)} ${remCalc(10)}`
}))

const NoteBox = styled(Box)(() => ({
    paddingTop: remCalc(15),
    paddingBottom: remCalc(15)
}))

const FieldName = styled(Box)(() => ({
    display: 'flex',
    justifyContent: 'flex-start',
    fontWeight: 'bolder',
    width: remCalc(120)
}))

const FieldValue = styled(Box)(() => ({
    display: 'flex',
    alignContent: 'flex-start',
    width: remCalc(180)
}))

const ExpandedContainer = styled(Box)(({ theme }) => ({
    alignContent: 'flex-start',
    display: 'flex',
    flexFlow: 'column wrap',
    color: theme.palette.text.primary,
    justifyContent: 'flex-start',
    height: remCalc(175),
    width: '100%',
    padding: `${remCalc(7)} 0 ${remCalc(7)} ${remCalc(10)}`
}))

const CloseButton = () => <CloseButtonStyled>
    <CloseIcon fontSize="small"/>
</CloseButtonStyled>

interface LogRowProps {
    logItem: TradeLog
}

interface ExpandableProps {
    logItem: TradeLog,
    expandHandler: (expanded: boolean) => void
}

const Expanded = ({ logItem, expandHandler }: ExpandableProps) => {
    const {
        broker, market, ticker, position, dateOpen, dateClose, currency,
        priceClose, priceOpen, itemNumber, outcomePercent, fees, volume, outcome,
        volumeToDeposit, stopLoss, takeProfit, risk, profit, goal, grade, note
    } = logItem

    const tz = market.timezone
    return <RowBox container columns={53}>
        <Grid item xs={40}>
            <ExpandedContainer>
                <FieldBox>
                    <FieldName>Broker:</FieldName>
                    <FieldValue>{broker.name}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Market:</FieldName>
                    <FieldValue>{market.name}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Ticker:</FieldName>
                    <FieldValue>{ticker.name} ({ticker.shortName})</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Position:</FieldName>
                    <FieldValue>{position}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Open on:</FieldName>
                    <FieldValue>{dateTimeWithOffset(dateOpen, tz)}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Closed on:</FieldName>
                    <FieldValue>{dateClose ? dateTimeWithOffset(dateClose, tz) : '-'}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Price open:</FieldName>
                    <FieldValue>{money(currency.name, priceOpen)}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Price close:</FieldName>
                    <FieldValue>{money(currency.name, priceClose)}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Items:</FieldName>
                    <FieldValue>{itemNumber}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Volume:</FieldName>
                    <FieldValue>{money(currency.name, volume)}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Volume/dep:</FieldName>
                    <FieldValue>{volumeToDeposit} %</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Stop loss:</FieldName>
                    <FieldValue>{money(currency.name, stopLoss)}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Take profit:</FieldName>
                    <FieldValue>{money(currency.name, takeProfit)}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Risk:</FieldName>
                    <FieldValue>{risk} %</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Fees:</FieldName>
                    <FieldValue>{money(currency.name, fees)}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Outcome:</FieldName>
                    <FieldValue>{money(currency.name, outcome)}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Pos. profit:</FieldName>
                    <FieldValue>{outcomePercent} %</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Cap. profit:</FieldName>
                    <FieldValue>{profit} %</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Goal:</FieldName>
                    <FieldValue>{money(currency.name, goal)}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Grade:</FieldName>
                    <FieldValue>{grade}</FieldValue>
                </FieldBox>
            </ExpandedContainer>
        </Grid>
        <Grid item xs={11}>
            <NoteBox>{note}</NoteBox>
        </Grid>
        <Item item sx={{ height: remCalc(36) }} xs={2}>
            <ExpandButton expanded={true} onClick={expandHandler}/>
            {dateClose ? <></> : <CloseButton/>}
        </Item>
    </RowBox>
}

const Collapsed = ({ logItem, expandHandler }: ExpandableProps) => {
    const {
        broker, market, ticker, position, dateOpen, dateClose, currency,
        priceClose, priceOpen, itemNumber, outcomePercent, fees, volume, outcome
    } = logItem

    const tz = market.timezone

    return <RowBox container columns={53}>
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
            <ExpandButton expanded={false} onClick={expandHandler}/>
            {dateClose ? <></> : <CloseButton/>}
        </Item>
    </RowBox>
}

export const LogRow = ({ logItem }: LogRowProps) => {
    const [expanded, setExpanded] = useState(false)
    const expandHandler = useCallback((expanded: boolean) => {
        setExpanded(expanded)
    }, [])
    return <>{expanded ? <Expanded logItem={logItem} expandHandler={expandHandler}/> :
        <Collapsed logItem={logItem} expandHandler={expandHandler}/>}</>
}
