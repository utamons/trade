import React, { Dispatch, useContext } from 'react'
import { Box, styled } from '@mui/material'
import { remCalc } from '../../utils/utils'
import { TradeContext } from '../../trade-context'
import { FormAction, FormActionPayload, FormState, ItemType } from 'types'
import Select from '../tools/select'
import { getFieldValue, useForm } from '../dialogs/dialogUtils'

const FilterContainer = styled(Box)(({ theme }) => ({
    display: 'flex',
    flexFlow: 'row',
    alignItems: 'center',
    color: theme.palette.text.primary,
    backgroundColor: theme.palette.background.default,
    justifyContent: 'flex-start',
    gap: remCalc(20),
    borderColor: theme.palette.text.primary,
    padding: `${remCalc(15)} ${remCalc(20)} 0 ${remCalc(20)}`
}))

const dateRangeItems: ItemType[] = [
    {
        id: 0,
        name: 'Week to date'
    },
    {
        id: 1,
        name: 'Last week'
    },
    {
        id: 2,
        name: 'Month to date'
    },
    {
        id: 3,
        name: 'Last month'
    },
    {
        id: 4,
        name: 'Quarter to date'
    },
    {
        id: 5,
        name: 'Last quarter'
    },
    {
        id: 6,
        name: 'Year to date'
    },
    {
        id: 7,
        name: 'Last year'
    }
]
const initFormState = (formState: FormState, dispatch: Dispatch<FormAction>) => {
    if (formState.isInitialized)
        return

    const payload: FormActionPayload = {
        valuesNumeric: [
            {
                name: 'brokerId',
                valid: true,
                value: 1
            },
            {
                name: 'currencyId',
                valid: true,
                value: 1
            },
            {
                name: 'dateRange',
                valid: true,
                value: 0
            }
        ]
    }

    dispatch({ type: 'init', payload })
}

const Stats = () => {
    const { brokers, currencies } = useContext(TradeContext)
    const { formState, dispatch } = useForm()

    initFormState(formState, dispatch)

    const brokerId = '' + getFieldValue('brokerId', formState)
    const currencyId = '' + getFieldValue('currencyId', formState)
    const dateRange = '' + getFieldValue('dateRange', formState)

    const brokerItems: ItemType[] = []
    const currencyItems: ItemType[] = []

    if (brokers) {
        brokerItems.push({
            id: 0,
            name: 'All brokers'
        })
        for (const broker of brokers) {
            brokerItems.push({
                id: broker.id,
                name: broker.name
            })
        }
    }

    if (currencies) {
        currencyItems.push({
            id: 0,
            name: 'All currencies'
        })
        for (const currency of currencies) {
            currencyItems.push({
                id: currency.id,
                name: currency.name
            })
        }
    }

    return <FilterContainer>
        <Select
            label="Broker"
            items={brokerItems}
            value={brokerId}
            name={'brokerId'}
            variant={'medium'}
            dispatch={dispatch} />
        <Select
            label="Currency"
            items={currencyItems}
            value={currencyId}
            name={'currencyId'}
            variant={'medium'}
            dispatch={dispatch} />
        <Select
            label="Date range"
            items={dateRangeItems}
            value={dateRange}
            name={'dateRange'}
            variant={'medium'}
            dispatch={dispatch} />
    </FilterContainer>
}

export default Stats
