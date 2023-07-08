import { ValueFieldBoxProps } from 'types'
import { FieldBox, FieldName, FieldValue } from '../../styles/style'
import { Grid } from '@mui/material'
import React from 'react'

export default ({ label, value, color } : ValueFieldBoxProps) => (
    <Grid item xs={1}>
        <FieldBox>
            <FieldName>{label}</FieldName>
            <FieldValue sx={color}>{value}</FieldValue>
        </FieldBox>
    </Grid>
)
