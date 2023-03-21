import React, { useCallback, useState } from 'react'
import TextField from '@mui/material/TextField'

interface NumberInputProps {
    value?: number
    onError: (error: boolean) => void
    onChange: (value: number) => void
    label?: string
    negativeAllowed?: boolean
    zeroAllowed?: boolean
}

export default ({ onError, onChange, label, negativeAllowed, zeroAllowed, value }: NumberInputProps) => {

    const [error, setError] = useState(false)
    const [errorText, setErrorText] = useState('')
    const [_value, setValue] = useState<string | number | undefined>(value)
    const [oldValue, setOldValue] = useState<string | number | undefined>(value)

    if (value && oldValue != value) {
        setValue(value)
        setOldValue(value)
    }

    console.log(_value)

    const handleInput = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
        setValue((event.target.value))
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
        if (!value ||
            isNaN(num) ||
            ((negativeAllowed == undefined || !negativeAllowed) && num < 0) ||
            ((zeroAllowed == undefined || !zeroAllowed) && num == 0)
        ) {
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
    />
}
