import React from 'react'
import { Box, styled } from '@mui/material'
import { remCalc } from '../../utils/utils'
import { SubMenu, SubMenuOption } from './subMenu'

export interface MainMenuItemProps {
    name: string,
    options: SubMenuOption[]
}

const Container = styled(Box)(({ theme }) => ({
    padding: remCalc(5),
    color: theme.palette.text.primary,
    '&.selected': {
        backgroundColor: theme.palette.text.disabled
    },
    '&:hover': {
        backgroundColor: theme.palette.action.hover,
        borderColor: theme.palette.text.primary,
        cursor: 'pointer'
    }
}))

export const MainMenuItem = ({ name, options }: MainMenuItemProps) => {
    const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null)
    const [selected, setSelected] = React.useState(false)

    const open = (event: React.MouseEvent<HTMLDivElement>) => {
        setAnchorEl(event.currentTarget)
        setSelected(true)
    }

    const close = () => {
        setAnchorEl(null)
        setSelected(false)
    }

    return (<>
            <Container className={selected ? 'selected' : ''} onClick={open}>
                {name}</Container>
            <SubMenu options={options} anchorEl={anchorEl} close={close}/>
        </>
    )
}
