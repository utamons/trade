import { Box, IconButton, styled } from '@mui/material'
import { RED, remCalc } from '../utils/utils'
import Switch from '@mui/material/Switch'

export const ButtonContainerStyled = styled(Box)(() => ({
    padding: remCalc(8),
    display: 'flex',
    flexFlow: 'row',
    alignItems: 'center',
    fontWeight: 'normal',
    justifyContent: 'space-between',
    gap: remCalc(10)
}))

export const FieldName = styled(Box)(() => ({
    display: 'flex',
    justifyContent: 'flex-start',
    fontWeight: 'bolder',
    fontFamily: 'sans-serif',
    fontSize: 'inherit',
    width: remCalc(120)
}))

export const SwitchBox = styled(Box)(() => ({
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    width: remCalc(150),
    height: remCalc(50)
}))

export const FieldValue = styled(Box)(() => ({
    display: 'flex',
    alignContent: 'flex-start',
    alignItems: 'center',
    width: remCalc(200)
}))

export const FieldBox = styled(Box)(() => ({
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    width: remCalc(300),
    height: remCalc(50)
}))

export const NoteBox = styled(Box)(() => ({
    width: '100%',
    marginTop: remCalc(10)
}))

export const IconButtonStyled = styled(IconButton)(({ theme }) => ({
    width: remCalc(25),
    height: remCalc(25),
    marginTop: remCalc(2),
    color: theme.palette.text.primary
}))

export const RedSwitch = styled(Switch)(() => ({
    '& .MuiSwitch-switchBase.Mui-checked': {
        color: RED,
        '&:hover': {
            backgroundColor: 'rgba(204, 51, 0, 0.2)'
        }
    },
    '& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track': {
        backgroundColor: RED
    }
}))
