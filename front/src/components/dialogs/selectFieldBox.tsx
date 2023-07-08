import { FieldBox, FieldName, FieldValue } from '../../styles/style'
import Select from '../tools/select'
import { Grid } from '@mui/material'
import React from 'react'
import { SelectFieldBoxProps } from 'types'

export default ({
    label,
    value,
    items,
    variant,
    fieldName,
    dispatch
}: SelectFieldBoxProps) => (
    <Grid item xs={1}>
        <FieldBox>
            <FieldName>{label}</FieldName>
            <FieldValue>
                <Select
                    items={items}
                    value={value}
                    name={fieldName}
                    dispatch={dispatch}
                    variant={variant}
                />
            </FieldValue>
        </FieldBox>
    </Grid>
)
