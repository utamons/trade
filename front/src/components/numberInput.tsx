import React, { useCallback, useState } from 'react'
import TextField from '@mui/material/TextField'

interface NumberInputProps {
    onError: (error: boolean) => void
    onChange: (value: number) => void
    label?: string
}

export default ({ onError, onChange, label }: NumberInputProps) => {

    const [error, setError] = useState(false)
    const [errorText, setErrorText] = useState('')

    const handleInput = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
        validate(event.target.value)
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
        variant="standard"
        onChange={handleInput}
        onBlur={handleValidate}
        error={error}
        helperText={errorText}
        inputProps={{ inputMode: 'numeric' }}
    />
}
