import { FormAction, FormOptions, FormState } from 'types'
import { useReducer } from 'react'

export const getFieldValue = (name: string, state: FormState): number | string | Date | undefined => {
    const numericField = state.valuesNumeric.find((field) => field.name === name)
    if (numericField) {
        return numericField.value
    }

    const stringField = state.valuesString.find((field) => field.name === name)
    if (stringField) {
        return stringField.value
    }

    const dateField = state.valuesDate.find((field) => field.name === name)
    if (dateField) {
        return dateField.value
    }

    return undefined
}

const formReducer = (state: FormState, action: FormAction): FormState => {
    switch (action.type) {
        case 'set': {
            const { name, valueNum, valueStr, valueDate, valid } = action.payload

            let updatedValuesNumeric = state.valuesNumeric
            let updatedValuesString = state.valuesString
            let updatedValuesDate = state.valuesDate


            updatedValuesNumeric = state.valuesNumeric.map((field) =>
                field.name === name ? {
                    ...field,
                    value: valueNum ? valueNum : field.value,
                    valid: valid || false
                } : field
            )

            updatedValuesString = state.valuesString.map((field) =>
                field.name === name ? {
                    ...field,
                    value: valueStr ? valueStr : field.value,
                    valid: valid || false
                } : field
            )

            updatedValuesDate = state.valuesDate.map((field) =>
                field.name === name ? {
                    ...field,
                    value: valueDate ? valueDate : field.value,
                    valid: valid || false
                } : field
            )

            const allFields = [...updatedValuesNumeric, ...updatedValuesString, ...updatedValuesDate]
            const isValid = allFields.every((field) => field.valid)

            return {
                ...state,
                isValid,
                valuesNumeric: updatedValuesNumeric,
                valuesString: updatedValuesString,
                valuesDate: updatedValuesDate
            }
        }
        case 'reset': {
            return {
                ...state,
                isValid: false,
                isInitialized: false,
                valuesNumeric: state.valuesNumeric.map((field) => ({ ...field, value: undefined, valid: true })),
                valuesString: state.valuesString.map((field) => ({ ...field, value: undefined, valid: true })),
                valuesDate: state.valuesDate.map((field) => ({ ...field, value: undefined, valid: true }))
            }
        }
        case 'init': {
            const { valuesNumeric, valuesDate, valuesString } = action.payload

            return {
                isValid: false,
                isInitialized: true,
                valuesNumeric: valuesNumeric || [],
                valuesDate: valuesDate || [],
                valuesString: valuesString || []
            }
        }
        case 'remove': {
            const newState = { ...state }
            const { name } = action.payload

            newState.valuesNumeric = newState.valuesNumeric.filter(field => field.name !== name)
            newState.valuesString = newState.valuesString.filter(field => field.name !== name)
            newState.valuesDate = newState.valuesDate.filter(field => field.name !== name)

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
        valuesNumeric: [],
        valuesString: [],
        valuesDate: []
    })
    return { formState: state, dispatch }
}
