import React, { useCallback, useContext, useState } from 'react'
import { Grid } from '@mui/material'
import { TradeLog, TradeLogPageType } from 'types'
import { LogHeader } from './logHeader'
import { LogRow } from './logRow'
import CloseDialog from './dialogs/closeDialog'
import { TradeContext } from '../../trade-context'

const getRows = (logPage: TradeLogPageType) => {
    const [openCloseDialog, setOpenCloseDialog] = useState(false)
    const [logItem, setLogItem] = useState<TradeLog | undefined>()

    const closeDialogUp = useCallback((logItem: TradeLog) => {
        setLogItem(logItem)
        setOpenCloseDialog(true)
    }, [])

    const closeDialogDown = useCallback(() => {
        setOpenCloseDialog(false)
    }, [])

    return <>
        {logPage.content.map(logItem => <LogRow closeDialog={closeDialogUp} key={logItem.id} logItem={logItem}/>)}
        {logItem ?
            <CloseDialog isOpen={openCloseDialog} close={close} onClose={closeDialogDown} position={logItem}/> : <></>}
    </>
}

export default () => {
    const { logPage } = useContext(TradeContext)
    return <Grid sx={{ width: '100%' }} container columns={53}>
        <LogHeader/>
        {logPage ? getRows(logPage) : <></>}
    </Grid>
}
