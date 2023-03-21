import React, { useCallback } from 'react'
import { useTheme } from '@emotion/react'
import { Box, Grid, styled } from '@mui/material'
import { FieldName } from '../../styles/style'
import { money, profitColor, remCalc, riskColor, utc2market } from '../../utils/utils'
import ExpandButton from '../tools/expandButton'
import { EditButton } from '../tools/editButton'
import { CloseButton } from '../tools/closeButton'
import { ExpandableProps } from 'types'
import { Item, RowBox } from './styles'

const FieldBox = styled(Box)(() => ({
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    width: remCalc(310),
    margin: `${remCalc(2)} ${remCalc(20)} ${remCalc(2)} ${remCalc(2)}`,
    padding: `${remCalc(7)} 0 ${remCalc(7)} ${remCalc(10)}`
}))

const FieldValue = styled(Box)(() => ({
    display: 'flex',
    alignContent: 'flex-start',
    width: remCalc(180)
}))

const NoteBox = styled(Box)(() => ({
    paddingTop: remCalc(15),
    paddingBottom: remCalc(15)
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

export const Expanded = ({ logItem, expandHandler, closeDialog, editDialog }: ExpandableProps) => {
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
