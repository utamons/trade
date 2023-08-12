import React, { useCallback } from 'react'
import { Box, Grid, styled } from '@mui/material'
import { FieldName } from '../../styles/style'
import { money, remCalc, utc2market } from '../../utils/utils'
import ExpandButton from '../tools/expandButton'
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

export const Expanded = ({ logItem, expandHandler, closeDialog }: ExpandableProps) => {
    const {
        broker, market, ticker, position, dateOpen, dateClose, currency,
        outcome, totalBought, totalSold, itemBought, risk, itemSold, note
    } = logItem

    const closeDialogHandler = useCallback(() => {
        closeDialog(logItem)
    }, [])

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
                    <FieldName>Items bought:</FieldName>
                    <FieldValue>{itemBought ?? 0}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Items sold:</FieldName>
                    <FieldValue>{itemSold ?? 0}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Total bought:</FieldName>
                    <FieldValue>{money(currency.name, totalBought ?? 0.0)}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Total sold:</FieldName>
                    <FieldValue>{money(currency.name, totalSold ?? 0.0)}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Risk:</FieldName>
                    <FieldValue>{money(currency.name, risk)}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Outcome:</FieldName>
                    <FieldValue>{money(currency.name, risk)}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>R/R:</FieldName>
                    <FieldValue>{money(currency.name, outcome?risk/outcome*100:0)}%</FieldValue>
                </FieldBox>
            </ExpandedContainer>
        </Grid>
        <Grid item xs={10.5}>
            <NoteBox>{note}</NoteBox>
        </Grid>
        <Item item sx={{ height: remCalc(36) }} xs={2.5}>
            <ExpandButton expanded={true} onClick={expandHandler}/>
            {dateClose ? <></> : <CloseButton onClick={closeDialogHandler}/>}
        </Item>
    </RowBox>
}
