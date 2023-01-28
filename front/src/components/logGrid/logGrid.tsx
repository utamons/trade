import React, { useCallback } from 'react'
import { Grid, IconButton, styled } from '@mui/material'
import { dateTimeWithOffset, remCalc } from '../../utils/utils'
import { TradeLogPageType } from 'types'
import ExpandButton from './expandButton'
import CloseIcon from '@mui/icons-material/Close'

export const CloseButtonStyled = styled(IconButton)(({ theme }) => ({
    width: remCalc(25),
    height: remCalc(25),
    marginTop: remCalc(2),
    color: theme.palette.text.primary
}))

const HeaderBox = styled(Grid)(({ theme }) => ({
    alignItems: 'center',
    display: 'flex',
    color: theme.palette.text.primary,
    backgroundColor: theme.palette.action.disabledBackground,
    justifyContent: 'center',
    margin: remCalc(2),
    paddingTop: remCalc(7),
    paddingBottom: remCalc(7)
}))

const RowBox = styled(Grid)(({ theme }) => ({
    borderBottom: `solid ${remCalc(1)}`,
    borderColor: theme.palette.divider,
    paddingTop: remCalc(7),
    paddingBottom: remCalc(7),
    width: '100%'
}))

const ItemBox = styled(Grid)(({ theme }) => ({
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

const getRows = (logPage: TradeLogPageType) => {
    const expandHandler = useCallback((expanded: boolean) => {
        console.log(expanded)
    }, [])

    return logPage.content.map(logItem => <RowBox key={logItem.id} container columns={53}>
            <Grid item xs={4}>
                <ItemBox>{logItem.broker.name}</ItemBox>
            </Grid>
            <Grid item xs={3}>
                <ItemBox>{logItem.market.name}</ItemBox>
            </Grid>
            <Grid item xs={3}>
                <ItemBox>{logItem.ticker.shortName}</ItemBox>
            </Grid>
            <Grid item xs={2}>
                <ItemBox>{logItem.position}</ItemBox>
            </Grid>
            <Grid item xs={6}>
                <ItemBox>{dateTimeWithOffset(logItem.dateOpen, logItem.market.timezone)}</ItemBox>
            </Grid>
            <Grid item xs={6}>
                <ItemBox>{logItem.dateClose ?
                    dateTimeWithOffset(logItem.dateClose, logItem.market.timezone) : '-'}</ItemBox>
            </Grid>
            <Grid item xs={3}>
                <ItemBox>{logItem.itemNumber}</ItemBox>
            </Grid>
            <Grid item xs={4}>
                <ItemBox>{logItem.priceOpen}</ItemBox>
            </Grid>
            <Grid item xs={4}>
                <ItemBox>{logItem.priceClose ? logItem.priceClose : '-'}</ItemBox>
            </Grid>
            <Grid item xs={4}>
                <ItemBox>{logItem.volume}</ItemBox>
            </Grid>
            <Grid item xs={4}>
                <ItemBox>{logItem.stopLoss}</ItemBox>
            </Grid>
            <Grid item xs={4}>
                <ItemBox>{logItem.takeProfit}</ItemBox>
            </Grid>
            <Grid item xs={4}>
                <ItemBox>{logItem.fees}</ItemBox>
            </Grid>
            <Grid item xs={2}>
                <ItemBox>
                    <ExpandButton onClick={expandHandler}/>
                    {logItem.dateClose?<></>:<CloseButton/>}
                </ItemBox>
            </Grid>
        </RowBox>
    )
}

export default (logPage: TradeLogPageType) => {
    return <Grid sx={{ width: '100%' }} container columns={53}>
        <Grid item xs={4}>
            <HeaderBox>Broker</HeaderBox>
        </Grid>
        <Grid item xs={3}>
            <HeaderBox>Market</HeaderBox>
        </Grid>
        <Grid item xs={3}>
            <HeaderBox>Ticker</HeaderBox>
        </Grid>
        <Grid item xs={2}>
            <HeaderBox>Position</HeaderBox>
        </Grid>
        <Grid item xs={6}>
            <HeaderBox>Open</HeaderBox>
        </Grid>
        <Grid item xs={6}>
            <HeaderBox>Closed</HeaderBox>
        </Grid>
        <Grid item xs={3}>
            <HeaderBox>Items</HeaderBox>
        </Grid>
        <Grid item xs={4}>
            <HeaderBox>Price Open</HeaderBox>
        </Grid>
        <Grid item xs={4}>
            <HeaderBox>Price Close</HeaderBox>
        </Grid>
        <Grid item xs={4}>
            <HeaderBox>Volume</HeaderBox>
        </Grid>
        <Grid item xs={4}>
            <HeaderBox>Stop</HeaderBox>
        </Grid>
        <Grid item xs={4}>
            <HeaderBox>Take</HeaderBox>
        </Grid>
        <Grid item xs={4}>
            <HeaderBox>Fees</HeaderBox>
        </Grid>
        <Grid item xs={2}>
            &nbsp;
        </Grid>
        {logPage ? getRows(logPage) : <></>}
    </Grid>
}
