import { TradeLog } from 'types'
import React, { useCallback, useState } from 'react'
import { Expanded } from './expanded'
import { Collapsed } from './collapsed'

interface LogRowProps {
    logItem: TradeLog,
    closeDialog: (logItem: TradeLog) => void
    editDialog: (logItem: TradeLog) => void
}

export const LogRow = ({ logItem, closeDialog, editDialog }: LogRowProps) => {
    const [expanded, setExpanded] = useState(false)
    const expandHandler = useCallback((expanded: boolean) => {
        setExpanded(expanded)
    }, [])

    return <>{expanded ? <Expanded editDialog={editDialog} closeDialog={closeDialog} logItem={logItem} expandHandler={expandHandler}/> :
        <Collapsed editDialog={editDialog} closeDialog={closeDialog} logItem={logItem} expandHandler={expandHandler}/>}</>
}
