import React, { Dispatch, useCallback, useEffect, useState } from 'react'
import TextField from '@mui/material/TextField'
import { FormAction } from 'types'

interface NumberInputProps {
    color?: string,
    value?: number
    label?: string
    name: string
    dispatch: Dispatch<FormAction>
    valid?: boolean
    errorText?: string
}

export default ({ color, name, dispatch, label, value, valid, errorText }: NumberInputProps) => {

    const [error, setError] = useState(false)
    const [_errorText, setErrorText] = useState(errorText)
    const [_value, setValue] = useState<string | number | undefined>()

    const isValid = () => {
        return valid ?? true
    }

    const handleInput = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
        setValue((event.target.value))
        const valueNum = Number(event.target.value)
        if (event.target.value != '' && !isNaN(valueNum))
            dispatch({ type: 'set', payload: { name, valueNum } })
    }, [])

    const handleValidate =
        useCallback((event: React.FocusEvent<HTMLInputElement | HTMLTextAreaElement>) => {
            validate(event.target.value)
        }, [valid, value])

    useEffect(() => {
        setValue(value)
        setError(!isValid())
        if (!isValid() && errorText)
            setErrorText(errorText)
    }, [value, valid])

    const validate = (value: string): boolean => {
        const num = Number(value)
        if (value && isNaN(num)) {
            setErrorText('Non-numeric value')
            setError(true)
            dispatch({ type: 'set', payload: { name, valid: false } })
            return true
        }
        setError(false)
        setErrorText('')
        dispatch({ type: 'set', payload: { name, valid: true } })
        return false
    }

    return <TextField
        label={label}
        sx={{
            '.MuiInput-input': {
                color
            }
        }
        }
        value={_value == undefined ? '' : _value + ''}
        variant="standard"
        onChange={handleInput}
        onBlur={handleValidate}
        error={error}
        helperText={_errorText}
    />
}
