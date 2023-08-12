import { useTheme } from '@emotion/react'
import React, { useCallback } from 'react'
import { GREEN, money, profitColor, remCalc, utc2market } from '../../utils/utils'
import ExpandButton from '../tools/expandButton'
import { CloseButton } from '../tools/closeButton'
import { Item, RowBox } from './styles'
import { ExpandableProps } from 'types'
import { styled } from '@mui/material'
import CircleIcon from '@mui/icons-material/Circle'

const CircleOpen = styled(CircleIcon)(() => ({
    fontSize: remCalc(10),
    color: GREEN,
    marginRight: remCalc(10)
}))

export const Collapsed = ({ logItem, expandHandler, closeDialog }: ExpandableProps) => {
    const {
        broker, market, ticker, position, dateOpen, dateClose, currency,
        outcome, itemBought, itemSold, totalSold, totalBought,
        outcomePc, openCommission, closeCommission, brokerInterest
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

    const openP = () => {
        return dateClose ? '' : <CircleOpen/>
    }

    const fees = (openCommission ?? 0) + (closeCommission ?? 0) + (brokerInterest ?? 0)

    return <RowBox container columns={53}>
        <Item item xs={4}>{openP()} {broker.name}</Item>
        <Item item xs={3}>{market.name}</Item>
        <Item item xs={3}>{ticker.name}</Item>
        <Item item xs={2}>{position}</Item>
        <Item item xs={6}>{utc2market(dateOpen, tz)}</Item>
        <Item item xs={6}>{dateClose ? utc2market(dateClose, tz) : '-'}</Item>
        <Item item xs={4}>{money(currency.name, itemBought ?? 0.0)}</Item>
        <Item item xs={4}>{money(currency.name, itemSold ?? 0.0)}</Item>
        <Item item xs={3}>{money(currency.name, totalBought ?? 0.0)}</Item>
        <Item item xs={4}>{money(currency.name, totalSold ?? 0.0)}</Item>
        <Item item sx={profitColor(outcome, defaultColor)} xs={4}>{money(currency.name, outcome)}</Item>
        <Item item sx={profitColor(outcomePc, defaultColor)}
              xs={4}>{outcomePc != undefined ? `${outcomePc} %` : '-'}</Item>
        <Item item xs={3.5}>{money('USD', fees)}</Item>
        <Item item xs={2.5}>
            <ExpandButton expanded={false} onClick={expandHandler}/>
            {dateClose ? <></> : <CloseButton onClick={closeDialogHandler}/>}
        </Item>
    </RowBox>
}
