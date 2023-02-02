import { Box, styled } from '@mui/material'
import { remCalc } from '../utils/utils'

export const SelectorContainerStyled = styled(Box)(() => ({
    padding: remCalc(20),
    display: 'flex',
    flexFlow: 'row',
    alignItems: 'center',
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

export const FieldBox = styled(Box)(() => ({
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    width: remCalc(310),
    margin: `${remCalc(2)} ${remCalc(20)} ${remCalc(2)} ${remCalc(2)}`,
    padding: `${remCalc(7)} 0 ${remCalc(7)} ${remCalc(10)}`
}))

export const NoteBox = styled(Box)(() => ({
    paddingTop: remCalc(15),
    paddingBottom: remCalc(15)
}))

export const FieldName = styled(Box)(() => ({
    display: 'flex',
    justifyContent: 'flex-start',
    fontWeight: 'bolder',
    fontFamily: 'sans',
    fontSize: 'inherit',
    width: remCalc(120)
}))

export const FieldValue = styled(Box)(() => ({
    display: 'flex',
    alignContent: 'flex-start',
    width: remCalc(180)
}))
