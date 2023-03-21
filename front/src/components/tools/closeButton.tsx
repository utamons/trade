import { CloseButtonProps } from 'types'
import CloseIcon from '@mui/icons-material/Close'
import React from 'react'
import { IconButtonStyled } from '../../styles/style'

export const CloseButton = ({ onClick }: CloseButtonProps) => <IconButtonStyled onClick={onClick}>
    <CloseIcon fontSize="small"/>
</IconButtonStyled>
