import { Box, Grid, IconButton, styled } from '@mui/material'
import { BLUE, GREEN, money, profitColor, remCalc, riskColor, utc2market } from '../../utils/utils'
import CloseIcon from '@mui/icons-material/Close'
import { CloseButtonProps, TradeLog } from 'types'
import React, { useCallback, useState } from 'react'
import ExpandButton from './expandButton'
import { FieldBox, FieldName, FieldValue, NoteBox } from '../../styles/style'
import { useTheme } from '@emotion/react'
import CircleIcon from '@mui/icons-material/Circle'
import EditIcon from '@mui/icons-material/Edit';
import editDialog from './editDialog'

export const CloseButtonStyled = styled(IconButton)(({ theme }) => ({
    width: remCalc(25),
    height: remCalc(25),
    marginTop: remCalc(2),
    color: theme.palette.text.primary
}))

export const CircleOpen = styled(CircleIcon)(() => ({
    fontSize: remCalc(10),
    color: GREEN,
    marginRight: remCalc(10)
}))

export const CircleChild = styled(CircleIcon)(() => ({
    fontSize: remCalc(10),
    color: BLUE,
    marginRight: remCalc(10)
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

const CloseButton = ({ onClick }: CloseButtonProps) => <CloseButtonStyled onClick={onClick}>
    <CloseIcon fontSize="small"/>
</CloseButtonStyled>

const EditButton = ({ onClick }: CloseButtonProps) => <CloseButtonStyled onClick={onClick}>
    <EditIcon fontSize="small"/>
</CloseButtonStyled>

interface LogRowProps {
    logItem: TradeLog,
    closeDialog: (logItem: TradeLog) => void
    editDialog: (logItem: TradeLog) => void
}

interface ExpandableProps {
    logItem: TradeLog,
    expandHandler: (expanded: boolean) => void
    closeDialog: (logItem: TradeLog) => void
    editDialog: (logItem: TradeLog) => void
}

const Expanded = ({ logItem, expandHandler, closeDialog, editDialog }: ExpandableProps) => {
    const {
        broker, market, ticker, position, dateOpen, dateClose, currency, brokerInterest,
        priceClose, priceOpen, itemNumber, outcomePercent, fees, volume, outcome,
        volumeToDeposit, stopLoss, takeProfit, risk, breakEven, profit, note
    } = logItem

    const closeDialogHandler = useCallback(() => {
        closeDialog(logItem)
    }, [])

    const editDialogHandler = useCallback(() => {
        editDialog(logItem)
    }, [])

    const theme = useTheme()
    // noinspection TypeScriptUnresolvedVariable
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    const defaultColor = theme.palette.text.primary

    const tz = market.timezone

    const breakEvenPc = () => {
        if (!breakEven) return '-'
            return (Math.abs(priceOpen - breakEven) / priceOpen * 100).toFixed(2)
    }

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
                    <FieldValue>{ticker.name} ({ticker.longName})</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Position:</FieldName>
                    <FieldValue>{position}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Open on:</FieldName>
                    <FieldValue>{utc2market(dateOpen, tz)}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Closed on:</FieldName>
                    <FieldValue>{dateClose ? utc2market(dateClose, tz) : '-'}</FieldValue>
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
                    <FieldValue sx={riskColor(risk, defaultColor)}>{risk} %</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Fees:</FieldName>
                    <FieldValue>{money('USD', fees)}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Break even</FieldName>
                    <FieldValue>{money(currency.name, breakEven)} ({breakEvenPc()}%)</FieldValue>
                </FieldBox>
                {position == 'short' ? <FieldBox>
                    <FieldName>Broker interest</FieldName>
                    <FieldValue>{money('USD', brokerInterest)}</FieldValue>
                </FieldBox> : <></>}
                <FieldBox>
                    <FieldName>Outcome:</FieldName>
                    <FieldValue sx={profitColor(outcome, defaultColor)}>{money(currency.name, outcome)}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Pos. profit:</FieldName>
                    <FieldValue sx={profitColor(outcomePercent, defaultColor)}>{outcomePercent} %</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Cap. profit:</FieldName>
                    <FieldValue sx={profitColor(profit, defaultColor)}>{profit} %</FieldValue>
                </FieldBox>
            </ExpandedContainer>
        </Grid>
        <Grid item xs={10.5}>
            <NoteBox>{note}</NoteBox>
        </Grid>
        <Item item sx={{ height: remCalc(36) }} xs={2.5}>
            <ExpandButton expanded={true} onClick={expandHandler}/>
            {dateClose ? <></> : <EditButton onClick={editDialogHandler}/>}
            {dateClose ? <></> : <CloseButton onClick={closeDialogHandler}/>}
        </Item>
    </RowBox>
}

const Collapsed = ({ logItem, expandHandler, closeDialog, editDialog }: ExpandableProps) => {
    const {
        broker, market, ticker, position, dateOpen, dateClose, currency,
        priceClose, priceOpen, itemNumber, outcomePercent, fees, volume, outcome,
        parentId
    } = logItem

    const tz = market.timezone

    const theme = useTheme()
    // noinspection TypeScriptUnresolvedVariable
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    const defaultColor = theme.palette.text.primary

    const closeDialogHandler = useCallback(() => {
        closeDialog(logItem)
    }, [])

    const editDialogHandler = useCallback(() => {
        editDialog(logItem)
    }, [])

    const openP = () => {
        return dateClose ? '' : <CircleOpen/>
    }

    const childP = () => {
        return parentId ? <CircleChild/> : ''
    }

    return <RowBox container columns={53}>
        <Item item xs={4}>{openP()}{childP()} {broker.name}</Item>
        <Item item xs={3}>{market.name}</Item>
        <Item item xs={3}>{ticker.name}</Item>
        <Item item xs={2}>{position}</Item>
        <Item item xs={6}>{utc2market(dateOpen, tz)}</Item>
        <Item item xs={6}>{dateClose ? utc2market(dateClose, tz) : '-'}</Item>
        <Item item xs={4}>{money(currency.name, priceOpen)}</Item>
        <Item item xs={4}>{priceClose ? money(currency.name, priceClose) : '-'}</Item>
        <Item item xs={3}>{itemNumber}</Item>
        <Item item xs={4}>{money(currency.name, volume)}</Item>
        <Item item sx={profitColor(outcome, defaultColor)} xs={4}>{money(currency.name, outcome)}</Item>
        <Item item sx={profitColor(outcomePercent, defaultColor)}
              xs={4}>{outcomePercent != undefined ? `${outcomePercent} %` : '-'}</Item>
        <Item item xs={3.5}>{money('USD', fees)}</Item>
        <Item item xs={2.5}>
            <ExpandButton expanded={false} onClick={expandHandler}/>
            {dateClose ? <></> : <EditButton onClick={editDialogHandler}/>}
            {dateClose ? <></> : <CloseButton onClick={closeDialogHandler}/>}
        </Item>
    </RowBox>
}

export const LogRow = ({ logItem, closeDialog, editDialog }: LogRowProps) => {
    const [expanded, setExpanded] = useState(false)
    const expandHandler = useCallback((expanded: boolean) => {
        setExpanded(expanded)
    }, [])

    return <>{expanded ? <Expanded editDialog={editDialog} closeDialog={closeDialog} logItem={logItem} expandHandler={expandHandler}/> :
        <Collapsed editDialog={editDialog} closeDialog={closeDialog} logItem={logItem} expandHandler={expandHandler}/>}</>
}
