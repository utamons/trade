import { useTheme } from '@emotion/react'
import React, { useCallback } from 'react'
import { BLUE, GREEN, money, profitColor, remCalc, utc2market } from '../../utils/utils'
import ExpandButton from '../tools/expandButton'
import { EditButton } from '../tools/editButton'
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

const CircleChild = styled(CircleIcon)(() => ({
    fontSize: remCalc(10),
    color: BLUE,
    marginRight: remCalc(10)
}))

export const Collapsed = ({ logItem, expandHandler, closeDialog, editDialog }: ExpandableProps) => {
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
