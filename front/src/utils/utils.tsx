import React from 'react'
import CircularProgress from '@mui/material/CircularProgress'

export const Loadable = ({ children, isLoading }: { children?: JSX.Element, isLoading?: boolean }): JSX.Element => (
    isLoading ? < CircularProgress size={20} /> : <>{children}</>
)
