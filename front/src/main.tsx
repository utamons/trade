import React from 'react'
import { Box, Container, Grid, styled } from '@mui/material'
import { fetchTest } from './api'
import { useQuery } from 'react-query'


const MainStyled = styled(Box)(() => ({
    alignItems: 'center',
    display: 'flex',
    color: 'black',
    fontSize: 22,
    fontWeight: 'bold',
    justifyContent: 'center',
    width: '100%'

}))
export default () => {

    const { data, isLoading } = useQuery('fetchTest', fetchTest, {
            refetchOnMount: false,
            refetchOnWindowFocus: false,
            refetchOnReconnect: false,
            refetchInterval: false as false,
            retry: 0
        }
    )
    return (
        <MainStyled>
            <Container>
                <Grid id="mainContainer" container spacing={0} className="content">
                    Test: {isLoading ? 'Loading' : data[0].text}
                </Grid>
            </Container>
        </MainStyled>
    )
}
