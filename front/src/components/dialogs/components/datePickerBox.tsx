import { DatePickerBoxProps } from 'types'
import { FieldBox, FieldName, FieldValue } from '../../../styles/style'
import { BasicDateTimePicker } from '../../tools/dateTimePicker'
import { Grid } from '@mui/material'
import React from 'react'

export default ({ label, fieldName, dispatch }: DatePickerBoxProps) => (
    <Grid item xs={1}>
        <FieldBox>
            <FieldName>{label}</FieldName>
            <FieldValue>
                <BasicDateTimePicker name={fieldName} dispatch={dispatch}/>
            </FieldValue>
        </FieldBox>
    </Grid>
)
