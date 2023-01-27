import { Box, styled } from '@mui/material'
import { remCalc } from '../utils/utils'

export const SelectorContainerStyled = styled(Box)(() => ({
    padding: remCalc(20),
    display: 'flex',
    flexFlow: 'row',
    alignItems: 'center',
    fontSize: remCalc(18),
    fontWeight: 'normal',
    justifyContent: 'center'
}))

export const ButtonContainerStyled = styled(Box)(() => ({
    padding: remCalc(8),
    display: 'flex',
    flexFlow: 'row',
    alignItems: 'center',
    fontWeight: 'normal',
    justifyContent: 'space-between',
    gap: remCalc(10)
}))
