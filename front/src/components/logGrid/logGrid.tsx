import React from 'react'
import { Grid } from '@mui/material'
import { TradeLogPageType } from 'types'
import { LogHeader } from './logHeader'
import { LogRow } from './logRow'


const getRows = (logPage: TradeLogPageType) => {
    return logPage.content.map(logItem => <LogRow key={logItem.id} logItem={logItem}/>)
}

export default (logPage: TradeLogPageType) => {
    return <Grid sx={{ width: '100%' }} container columns={53}>
        <LogHeader />
        {logPage ? getRows(logPage) : <></>}
    </Grid>
}
