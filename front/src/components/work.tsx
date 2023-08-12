import React, { useCallback, useContext, useState } from 'react'
import { Box, styled } from '@mui/material'
import { remCalc } from '../utils/utils'
import Button from './tools/button'
import { ButtonContainerStyled } from '../styles/style'
import LogGrid from './logGrid/logGrid'
import { TradeContext } from '../trade-context'
import Paginator from './logGrid/paginator'
import CircularProgress from '@mui/material/CircularProgress'
import Open from './logGrid/dialogs/openDialog'

const ContainerStyled = styled(Box)(({ theme }) => ({
    alignItems: 'top',
    flexFlow: 'column',
    display: 'flex',
    color: theme.palette.text.primary,
    backgroundColor: theme.palette.background.default,
    fontWeight: 'normal',
    justifyContent: 'left',
    padding: remCalc(20),
    gap: remCalc(3)
}))

const RowStyled = styled(Box)(() => ({
    alignItems: 'left',
    display: 'flex',
    justifyContent: 'left',
    width: '100%'
}))

const WorkInt = () => {
    const [isOpen, setOpen] = useState(false)

    const handleOpen = useCallback(() => {
        setOpen(true)
    }, [])

    const handleCloseOpenDialog = useCallback(() => {
        setOpen(false)
    }, [])

    return <ContainerStyled>
        <RowStyled>
            <ButtonContainerStyled>
                <Button style={{ minWidth: remCalc(101) }} text="Open" onClick={handleOpen}/>
            </ButtonContainerStyled>
        </RowStyled>
        <RowStyled>
            <LogGrid />
        </RowStyled>
        <RowStyled>
            <Paginator/>
        </RowStyled>
        <Open
            isOpen={isOpen}
            onClose={handleCloseOpenDialog}/>
    </ContainerStyled>
}

export default () => {
    const { isLoading } = useContext(TradeContext)
    if (isLoading)
        return <CircularProgress size={20}/>

    return <WorkInt />
}
