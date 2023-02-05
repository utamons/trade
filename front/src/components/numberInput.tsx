import React, { useCallback, useState } from 'react'
import TextField from '@mui/material/TextField'

interface NumberInputProps {
    value?: number
    onError: (error: boolean) => void
    onChange: (value: number) => void
    label?: string
}

export default ({ onError, onChange, label, value }: NumberInputProps) => {

    const [error, setError] = useState(false)
    const [errorText, setErrorText] = useState('')
    const [_value, setValue] = useState<string | number | undefined>(value)


    const handleInput = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
        console.log(event.target.value)
        setValue((event.target.value))
        validate(event.target.value)
        const valueNum = Number(event.target.value)
        if (event.target.value != '' && !isNaN(valueNum))
            onChange(Number(event.target.value))
    }, [])

    const handleValidate =
        useCallback((event: React.FocusEvent<HTMLInputElement | HTMLTextAreaElement>) => {
            validate(event.target.value)
        }, [])

    const validate = (value: string): boolean => {
        const num = Number(value)
        if (!value || isNaN(num)) {
            setErrorText('Incorrect value')
            setError(true)
            onError(true)
            return true
        }
        setError(false)
        setErrorText('')
        onError(false)
        return false
    }

    return <TextField
        label={label}
        value={_value == undefined ? '' : _value + ''}
        variant="standard"
        onChange={handleInput}
        onBlur={handleValidate}
        error={error}
        helperText={errorText}
        inputProps={{ inputMode: 'numeric' }}
    />
}
