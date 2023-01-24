import { Box, Grid, styled } from '@mui/material'
import { remCalc } from '../utils/utils'

const ContainerStyled = styled(Box)(({ theme }) => ({
    alignItems: 'top',
    display: 'flex',
    color: theme.palette.text.primary,
    backgroundColor: theme.palette.background.default,
    fontSize: remCalc(18),
    fontWeight: 'normal',
    justifyContent: 'left',
    width: '100%'
}))

export default () => {
    return <ContainerStyled>
        Work area. Under construction.
    </ContainerStyled>
}
