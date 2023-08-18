import Checkbox from '@mui/material/Checkbox'
import { FieldBox, FieldName } from '../../../styles/style'
import { Grid } from '@mui/material'
import React from 'react'

interface CheckFieldBoxProps {
    label: string
    checked: boolean
    onChange: (checked: boolean) => void
}

export default ({ label, checked, onChange }: CheckFieldBoxProps) => {
    const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        onChange(event.target.checked)
    }

    return <Grid item xs={1}>
        <FieldBox sx={{ justifyContent: 'flex-start' }}>
            <FieldName>{label}</FieldName>
            <Checkbox
                checked={checked}
                onChange={handleChange}
                inputProps={{ 'aria-label': 'controlled' }}
            />
        </FieldBox>
    </Grid>
}
