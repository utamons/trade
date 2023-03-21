import { CloseButtonProps } from 'types'
import EditIcon from '@mui/icons-material/Edit'
import React from 'react'
import { IconButtonStyled } from '../../styles/style'

export const EditButton = ({ onClick }: CloseButtonProps) => <IconButtonStyled onClick={onClick}>
    <EditIcon fontSize="small"/>
</IconButtonStyled>
