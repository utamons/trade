import { TradeLog } from 'types'
import React, { useCallback, useState } from 'react'
import { Expanded } from './expanded'
import { Collapsed } from './collapsed'

interface LogRowProps {
    logItem: TradeLog,
    closeDialog: (logItem: TradeLog) => void
}

export const LogRow = ({ logItem, closeDialog }: LogRowProps) => {
    const [expanded, setExpanded] = useState(false)
    const expandHandler = useCallback((expanded: boolean) => {
        setExpanded(expanded)
    }, [])

    return <>{expanded ? <Expanded closeDialog={closeDialog} logItem={logItem} expandHandler={expandHandler}/> :
        <Collapsed closeDialog={closeDialog} logItem={logItem} expandHandler={expandHandler}/>}</>
}
