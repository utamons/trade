import React from 'react'
import { Box, styled } from '@mui/material'

const ContainerStyled = styled(Box)(({ theme }) => ({
    alignItems: 'top',
    display: 'flex',
    color: theme.palette.text.primary,
    backgroundColor: theme.palette.background.default,
    fontWeight: 'normal',
    justifyContent: 'left',
    width: '100%'
}))

export default () => {
    return <ContainerStyled>
        Work area. Under construction.
    </ContainerStyled>
}
