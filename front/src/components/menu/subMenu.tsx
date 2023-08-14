import React from 'react'
import Menu from '@mui/material/Menu'
import MenuItem from '@mui/material/MenuItem'

interface SubMenuProps {
    anchorEl: HTMLElement | null
    options: SubMenuOption[]
    close: () => void
}

export interface SubMenuOption {
    name: string
    onClick: () => void
}

export const SubMenu = ({ anchorEl, close, options }: SubMenuProps) => {
    const open = Boolean(anchorEl)

    const handleMenuItemClick = (event: React.MouseEvent<HTMLElement>, index: number) => {
        console.log('handleMenuItemClick', index)
        options[index].onClick()
    }

    return (
        <Menu anchorEl={anchorEl} open={open} onClose={close}>
            {options.map((option, index) => (
                <MenuItem
                    key={option.name}
                    onClick={(event) => handleMenuItemClick(event, index)}
                >
                    {option.name}
                </MenuItem>
            ))}
        </Menu>
    )
}
