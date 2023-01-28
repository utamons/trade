import ExpandLessIcon from '@mui/icons-material/ExpandLess'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import React, { useCallback, useState } from 'react'
import { IconButton, styled } from '@mui/material'
import { remCalc } from '../../utils/utils'
import { ExpandButtonProps } from 'types'

export const ExpandButtonStyled = styled(IconButton)(({ theme }) => ({
    width: remCalc(20),
    height: remCalc(20),
    marginTop: remCalc(2),
    color: theme.palette.text.primary
}))


export default ({ onClick }: ExpandButtonProps) => {
    const [isExpanded, setExpanded] = useState(false)

    const onClickHandler = useCallback(
        () => {
            setExpanded(!isExpanded)
            onClick(!isExpanded)
        },
        [isExpanded]
    )

    return <ExpandButtonStyled onClick={onClickHandler}>
        {isExpanded ? <ExpandLessIcon/> : <ExpandMoreIcon/>}
    </ExpandButtonStyled>
}
