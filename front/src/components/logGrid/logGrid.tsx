import React, { useCallback, useState } from 'react'
import { Grid } from '@mui/material'
import { PositionCloseType, PositionEditType, TradeLog, TradeLogPageType } from 'types'
import { LogHeader } from './logHeader'
import { LogRow } from './logRow'
import CloseDialog from './dialogs/closeDialog'
import EditDialog from './dialogs/editDialog'

interface LogGridProps {
    logPage: TradeLogPageType,
    edit: (position: PositionEditType) => void
    close: (position: PositionCloseType) => void
}

const getRows = ({ logPage, close, edit }: LogGridProps) => {
    const [openCloseDialog, setOpenCloseDialog] = useState(false)
    const [openEditDialog, setOpenEditDialog] = useState(false)
    const [logItem, setLogItem] = useState<TradeLog | undefined>()

    const closeDialogUp = useCallback((logItem: TradeLog) => {
        setLogItem(logItem)
        setOpenCloseDialog(true)
    }, [])

    const closeDialogDown = useCallback(() => {
        setOpenCloseDialog(false)
    }, [])

    const editDialogUp = useCallback((logItem: TradeLog) => {
        setLogItem(logItem)
        setOpenEditDialog(true)
    }, [])

    const editDialogDown = useCallback(() => {
        setOpenEditDialog(false)
    }, [])

    return <>
        {logPage.content.map(logItem => <LogRow
            editDialog={editDialogUp}
            closeDialog={closeDialogUp} key={logItem.id} logItem={logItem}/>)}
        {logItem?<CloseDialog isOpen={openCloseDialog} close={close} onClose={closeDialogDown} position={logItem} />:<></>}
        {logItem?<EditDialog isOpen={openEditDialog} edit={edit} onClose={editDialogDown} position={logItem} />:<></>}
    </>
}

export default ( props: LogGridProps) => {
    return <Grid sx={{ width: '100%' }} container columns={53}>
        <LogHeader/>
        {props.logPage ? getRows(props) : <></>}
    </Grid>
}
