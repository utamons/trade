import React from 'react'
import Menu from '@mui/material/Menu'
import MenuItem from '@mui/material/MenuItem'
import Check from '@mui/icons-material/Check'
import { styled } from '@mui/material'
import { remCalc } from '../../utils/utils'

interface SubMenuProps {
    anchorEl: HTMLElement | null
    options: SubMenuOption[]
    close: () => void
}

export interface SubMenuOption {
    name: string
    checked?: boolean
    onClick: () => void
}

const CheckStyled = styled(Check)(() => ({
    fontSize: remCalc(14),
    paddingRight: remCalc(7)
}))

export const SubMenu = ({ anchorEl, close, options }: SubMenuProps) => {
    const open = Boolean(anchorEl)

    const handleMenuItemClick = (_event: React.MouseEvent<HTMLElement>, index: number) => {
        options[index].onClick()
        close()
    }

    return (
        <Menu anchorEl={anchorEl} open={open} onClose={close}>
            {options.map((option, index) => (
                <MenuItem
                    key={option.name}
                    onClick={(event) => handleMenuItemClick(event, index)}>
                    {option.checked ? <CheckStyled/>: null}{option.name}
                </MenuItem>
            ))}
        </Menu>
    )
}
