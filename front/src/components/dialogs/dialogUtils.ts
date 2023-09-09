import { FormAction, FormField, FormOptions, FormState } from 'types'
import { useReducer } from 'react'
import { roundTo2 } from '../../utils/utils'

export const getFieldValue = (name: string, state: FormState): number | string | Date | undefined => {
    const field = state.values.find((field) => field.name === name)
    if (field) {
        return field.value
    }

    return undefined
}

export const getFieldErrorText = (name: string, state: FormState): string | undefined => {
    const field = state.values.find((field) => field.name === name)
    if (field) {
        return field.errorText
    }
    return undefined
}

export const isFieldValid = (name: string, state: FormState): boolean => {
    const field = state.values.find((field) => field.name === name)
    if (field) {
        return field.valid
    }
    return true
}

const reset = (values: FormField[],
    payloadValues: FormField[])  => {
    return values.map((field) => {
        const payloadField = payloadValues.find((pf: FormField) => pf.name === field.name)
        if (payloadField) {
            return {
                ...field,
                value: payloadField.value,
                valid: payloadField.valid,
                errorText: payloadField.errorText
            }
        }
        return {
            ...field,
            value: undefined,
            valid: true,
            errorText: undefined
        }
    })
}

const formReducer = (state: FormState, action: FormAction): FormState => {
    console.log('formReducer', action)
    switch (action.type) {
        case 'clearErrors': {
            const updatedValues = state.values.map((field) => ({
                ...field,
                valid: true,
                errorText: undefined
            }))
            return {
                ...state,
                isValid: true,
                values: updatedValues
            }
        }
        case 'set': {
            const { name, value, valid, errorText } = action.payload

            let updatedValues = state.values

            updatedValues = state.values.map((field) =>
                field.name === name ? {
                    ...field,
                    value: (value && typeof value == 'number') ? (roundTo2(value) ?? value) : value,
                    valid: valid ?? true,
                    errorText: errorText
                } : field
            )

            const isValid = updatedValues.every((field) => field.valid)

            return {
                ...state,
                isValid,
                values: updatedValues
            }
        }
        case 'reset': {
            return {
                ...state,
                values: action.payload.values ?
                    reset(state.values, action.payload.values) :
                    state.values.map((field) => ({ ...field, value: undefined, valid: true }))
            }
        }
        case 'init': {
            const { values } = action.payload

            return {
                isValid: true,
                isInitialized: true,
                values: values || []
            }
        }
        case 'remove': {
            const newState = { ...state }
            const { name } = action.payload

            newState.values = newState.values.filter(field => field.name !== name)
            return newState
        }
        default:
            throw new Error(`Unsupported action type: ${action.type}`)
    }
}

export const useForm = (): FormOptions => {
    const [state, dispatch] = useReducer(formReducer, {
        isInitialized: false,
        isValid: true,
        values: []
    })
    return { formState: state, dispatch }
}
