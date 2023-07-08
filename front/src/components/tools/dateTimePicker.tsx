import React, { Dispatch, useCallback } from 'react'
import dayjs, { Dayjs } from 'dayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { DesktopDateTimePicker } from '@mui/x-date-pickers'
import { FormAction } from 'types'
import { styled } from '@mui/material'
import TextField from '@mui/material/TextField'
import { remCalc } from '../../utils/utils'

interface DatePickerProps {
    name: string,
    dispatch: Dispatch<FormAction>
}

const TextFieldStyled = styled(TextField)(() => ({
    '.MuiOutlinedInput-root': {
        borderRadius: remCalc(2),
        fontSize: remCalc(14),
        maxHeight: remCalc(38),
        maxWidth: remCalc(170)
    },
    '.MuiSvgIcon-root ': {
        fontSize: remCalc(18)
    }
}))

export const BasicDateTimePicker = ({ name, dispatch }: DatePickerProps) => {
    const [value, setValue] = React.useState<Dayjs | null>(dayjs(new Date()))

    const handleChange = useCallback((value: Dayjs | null) => {
        if (value) {
            setValue(value)
            dispatch({ type: 'set', payload: { name, valueDate: value.toDate() } })
        }
    }, [])

    return (
        <LocalizationProvider dateAdapter={AdapterDayjs}>
            <DesktopDateTimePicker
                renderInput={(props) => <TextFieldStyled {...props} />}
                value={value}
                inputFormat="YYYY-MM-DD HH:mm"
                onChange={handleChange}
                onAccept={handleChange}
            />
        </LocalizationProvider>
    )
}
