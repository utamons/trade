import React, { useCallback } from 'react'
import dayjs, { Dayjs } from 'dayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { DesktopDateTimePicker } from '@mui/x-date-pickers'
import { TextFieldStyled } from '../logGrid/dialogs/openDialog'

interface DatePickerProps {
    onChange: (date: Date) => void
}

export const BasicDateTimePicker = ({ onChange }: DatePickerProps) => {
    const [value, setValue] = React.useState<Dayjs | null>(dayjs(new Date()))

    const handleChange = useCallback((value: Dayjs | null) => {
        if (value) {
            setValue(value)
            onChange(value.toDate())
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
