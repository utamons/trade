import { ValueFieldBoxProps } from 'types'
import { FieldBox, FieldName, FieldValue } from '../../styles/style'
import { Grid } from '@mui/material'
import React from 'react'

export default ({ label, value, variant, color } : ValueFieldBoxProps) => (
    <Grid item xs={1}>
        <FieldBox>
            <FieldName>{label}</FieldName>
            <FieldValue sx={color}>{value ?? ''}{variant == 'pc'?' %':''}</FieldValue>
        </FieldBox>
    </Grid>
)
