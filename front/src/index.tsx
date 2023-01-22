import * as React from 'react'
import * as ReactDOM from 'react-dom'

import './styles/global.css'
import Main from './main'
import theme from './styles/theme'
import { ThemeProvider } from '@mui/material/styles'
import { QueryClient, QueryClientProvider } from 'react-query'
import { TradeProvider } from './utils/trade-context'

const Outer = () => {
    const queryClient = new QueryClient()
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
