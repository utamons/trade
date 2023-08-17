import { ValueFieldBoxProps } from 'types'
import { Box, Grid, styled } from '@mui/material'
import React from 'react'
import { remCalc } from '../../utils/utils'

export const FieldValue = styled(Box)(() => ({
    display: 'flex',
    alignContent: 'flex-start',
    alignItems: 'center',
    width: remCalc(60)
}))

export const FieldBox = styled(Box)(() => ({
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    height: remCalc(30),
    width: 'fit-content'
}))

export const FieldName = styled(Box)(() => ({
    display: 'flex',
    justifyContent: 'flex-start',
    fontWeight: 'bolder',
    fontFamily: 'sans-serif',
    fontSize: 'inherit',
    width: remCalc(230)
}))

export default ({ label, value, variant, color } : ValueFieldBoxProps) => (
    <Grid item xs={1}>
        <FieldBox>
            <FieldName>{label}</FieldName>
            <FieldValue sx={color}>{value ?? ''}{variant == 'pc'?' %':''}</FieldValue>
        </FieldBox>
    </Grid>
)