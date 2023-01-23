import { Box, Grid, styled } from '@mui/material'
import { remCalc } from '../utils/utils'

const ContainerStyled = styled(Box)(({theme}) => ({
    alignItems: 'top',
    display: 'flex',
    color: theme.palette.text.primary,
    backgroundColor: theme.palette.background.default,
    fontSize: remCalc(18),
    fontWeight: 'normal',
    justifyContent: 'left',
    width: '100%'
}))

const items = [
    "item1",
    "item2",
    "item3",
    "item4"
]

export default () => {
    return <ContainerStyled>
        <Grid container >
            {items.map(item=>
                <Grid key={item} item sx={{paddingBottom: remCalc(50)}} xs={12}>
                    {item}
                </Grid>)
            }
        </Grid>
    </ContainerStyled>
}
