import React, { useCallback, useContext } from 'react'
import { Box, styled } from '@mui/material'
import { Loadable, remCalc } from '../utils/utils'
import Button from './button'
import { ButtonContainerStyled } from '../styles/style'
import LogGrid from './logGrid/logGrid'
import { TradeContext } from '../trade-context'
import Paginator from './logGrid/paginator'

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

export default () => {
    const { all } = useContext(TradeContext)
    if (!all)
        return <></>
    const { isLoading, logPage } = all

    const handleAdd = useCallback(() => {
        console.log('Add')
    }, [])

    const handleEvaluate = useCallback(() => {
        console.log('Evaluate')
    }, [])

    return <ContainerStyled>
        <RowStyled>
            <ButtonContainerStyled>
                <Button style={{ minWidth: remCalc(101) }} text="Add" onClick={handleAdd}/>
                <Button style={{ minWidth: remCalc(101) }} text="Evaluate" onClick={handleEvaluate}/>
            </ButtonContainerStyled>
        </RowStyled>
        <RowStyled>
            <Loadable isLoading={isLoading}>
                {logPage? <LogGrid {...logPage}/> : <></>}
            </Loadable>
        </RowStyled>
        <RowStyled>
            <Paginator />
        </RowStyled>
    </ContainerStyled>
}
