import React from 'react'
import { styled, Button } from '@mui/material'
import { remCalc } from '../utils/utils'

const ButtonStyled = styled(Button)(({ theme }) => ({
    color: theme.palette.text.primary,
    paddingTop: remCalc(2),
    paddingBottom: remCalc(2),
    textTransform: 'initial',
    fontSize: remCalc(12),
    letterSpacing: remCalc(1),
    border: `solid ${remCalc(1)}`,
    borderRadius: remCalc(5),
    backgroundColor: theme.palette.action.disabledBackground,
    '&:hover': {
        color: theme.palette.text.primary,
        backgroundColor: theme.palette.action.hover,
        borderColor: theme.palette.text.primary,
    }
}))

export default ({ text, onClick, style={} }) => {

    return <ButtonStyled sx={style} variant="outlined" onClick={onClick}>
        {text}
    </ButtonStyled>
}
