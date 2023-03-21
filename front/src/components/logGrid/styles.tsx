import { Grid, styled } from '@mui/material'
import { remCalc } from '../../utils/utils'

export const RowBox = styled(Grid)(({ theme }) => ({
    borderBottom: `solid ${remCalc(1)}`,
    borderColor: theme.palette.divider,
    paddingTop: remCalc(7),
    paddingBottom: remCalc(7),
    width: '100%'
}))

export const Item = styled(Grid)(() => ({
    alignItems: 'center',
    display: 'flex',
    justifyContent: 'flex-start',
    padding: `${remCalc(7)} 0 ${remCalc(7)} ${remCalc(10)}`
}))
