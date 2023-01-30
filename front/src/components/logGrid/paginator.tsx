import React, { useCallback, useContext, useState } from 'react'
import { Box, IconButton, styled } from '@mui/material'
import { TradeContext } from '../../trade-context'
import { remCalc } from '../../utils/utils'
import FirstPageIcon from '@mui/icons-material/FirstPage'
import LastPageIcon from '@mui/icons-material/LastPage'
import KeyboardDoubleArrowLeftIcon from '@mui/icons-material/KeyboardDoubleArrowLeft'
import KeyboardDoubleArrowRightIcon from '@mui/icons-material/KeyboardDoubleArrowRight'

export const ButtonStyled = styled(IconButton)(({ theme }) => ({
    width: remCalc(20),
    height: remCalc(20),
    marginTop: remCalc(2),
    color: theme.palette.text.primary
}))

interface ButtonProps {
    onClick: () => void
}

const FirstPageBtn = ({ onClick }: ButtonProps) => {
    return <ButtonStyled onClick={onClick}>
        <FirstPageIcon/>
    </ButtonStyled>
}

const LastPageBtn = ({ onClick }: ButtonProps) => {
    return <ButtonStyled onClick={onClick}>
        <LastPageIcon/>
    </ButtonStyled>
}

const PrevPageBtn = ({ onClick }: ButtonProps) => {
    return <ButtonStyled onClick={onClick}>
        <KeyboardDoubleArrowLeftIcon/>
    </ButtonStyled>
}

const NextPageBtn = ({ onClick }: ButtonProps) => {
    return <ButtonStyled onClick={onClick}>
        <KeyboardDoubleArrowRightIcon/>
    </ButtonStyled>
}

const Container = styled(Box)(() => ({
    display: 'flex',
    alignItems: 'center',
    height: remCalc(50),
    gap: remCalc(2)
}))

const InfoBox = styled(Box)(() => ({
    display: 'flex',
    alignItems: 'center',
    height: remCalc(50),
    gap: remCalc(5),
    padding: `${remCalc(2)} ${remCalc(10)} 0 ${remCalc(10)}`
}))

interface PaginatorProps {
    first: boolean,
    last: boolean,
    totalPages: number,
    number: number,
    page: (page: number) => void
}

const PaginatorInt = ({ first, last, totalPages, number, page }: PaginatorProps) => {
    const firstPageClick = useCallback(() => {
        page(0)
    }, [])
    const lastPageClick = useCallback(() => {
        page(totalPages - 1)
    }, [])
    const nextPageClick = useCallback(() => {
        page(number + 1)
    }, [])
    const prevPageClick = useCallback(() => {
        page(number - 1)
    }, [])

    return <Container>
        {first ? <></> : <FirstPageBtn onClick={firstPageClick}/>}
        {first ? <></> : <PrevPageBtn onClick={prevPageClick}/>}
        <InfoBox>{number + 1} from {totalPages}</InfoBox>
        {last ? <></> : <NextPageBtn onClick={nextPageClick}/>}
        {last ? <></> : <LastPageBtn onClick={lastPageClick}/>}
    </Container>
}

export default () => {
    const { all } = useContext(TradeContext)
    if (!all)
        return <></>
    const { logPage, page } = all
    if (!logPage)
        return <></>

    const { first, last, totalPages, number, empty } = logPage

    if (empty || totalPages == 1)
        return <></>

    return <PaginatorInt number={number} first={first} last={last} page={page} totalPages={totalPages}/>
}
