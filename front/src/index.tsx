import * as React from 'react'
import * as ReactDOM from 'react-dom'

import './styles/global.css'
import Main from './main'
import { createTheme, ThemeProvider } from '@mui/material/styles'
import useMediaQuery from '@mui/material/useMediaQuery'
import { QueryClient, QueryClientProvider } from 'react-query'
import { TradeProvider } from './trade-context'

const Outer = () => {
    const queryClient = new QueryClient()
    const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)')

    const theme = React.useMemo(
        () =>
            createTheme({
                palette: {
                    mode: prefersDarkMode ? 'dark' : 'light'
                },
                typography: {
                    caption: {
                        fontSize: 14,
                        fontFamily: 'sans-serif'
                    }
                }
            }),
        [prefersDarkMode]
    )
    return <ThemeProvider theme={theme}>
        <QueryClientProvider client={queryClient}>
            <TradeProvider>
                <Main/>
            </TradeProvider>
        </QueryClientProvider>
    </ThemeProvider>
}

const container = document.getElementById('root')
if (container) {
    ReactDOM.render(<Outer/>, container)
}
