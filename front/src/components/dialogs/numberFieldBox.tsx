import { NumberFieldBoxProps } from 'types'
import { Grid } from '@mui/material'
import { FieldBox, FieldName, FieldValue } from '../../styles/style'
import NumberInput from '../tools/numberInput'
import React from 'react'

export default ({
    fieldName,
    label,
    value,
    zeroAllowed,
    color,
    dispatch
}: NumberFieldBoxProps) => (
    <Grid item xs={1}>
        <FieldBox>
            <FieldName>
                {label}
            </FieldName>
            <FieldValue>
                <NumberInput
                    zeroAllowed={zeroAllowed}
                    color={color}
                    value={value}
                    name={fieldName}
                    dispatch={dispatch}/>
            </FieldValue>
        </FieldBox>
    </Grid>
)
