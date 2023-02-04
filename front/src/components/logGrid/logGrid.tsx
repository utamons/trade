import React, { useCallback, useState } from 'react'
import { Grid } from '@mui/material'
import { PositionCloseType, TradeLog, TradeLogPageType } from 'types'
import { LogHeader } from './logHeader'
import { LogRow } from './logRow'
import CloseDialog from './closeDialog'

interface LogGridProps {
    logPage: TradeLogPageType,
    close: (position: PositionCloseType) => void
}

const getRows = ({ logPage, close }: LogGridProps) => {
    const [openDialog, setOpenDialog] = useState(false)
    const [logItem, setLogItem] = useState<TradeLog | undefined>()

    const closeDialogUp = useCallback((logItem: TradeLog) => {
        setLogItem(logItem)
        setOpenDialog(true)
    }, [])

    const closeDialogDown = useCallback(() => {
        setOpenDialog(false)
    }, [])

    return <>
        {logPage.content.map(logItem => <LogRow closeDialog={closeDialogUp} key={logItem.id} logItem={logItem}/>)}
        {logItem?<CloseDialog isOpen={openDialog} close={close} onClose={closeDialogDown} position={logItem} />:<></>}
    </>
}

export default ( props: LogGridProps) => {
    return <Grid sx={{ width: '100%' }} container columns={53}>
        <LogHeader/>
        {props.logPage ? getRows(props) : <></>}
    </Grid>
}
