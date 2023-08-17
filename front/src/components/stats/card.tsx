import CardHeader from '@mui/material/CardHeader'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import Collapse from '@mui/material/Collapse'
import CardContent from '@mui/material/CardContent'
import Card from '@mui/material/Card'
import React, { useState } from 'react'
import IconButton, { IconButtonProps } from '@mui/material/IconButton'
import { styled } from '@mui/material'
import { remCalc } from '../../utils/utils'

interface ExpandMoreProps extends IconButtonProps {
    expand: boolean;
}

const ExpandMore = styled((props: ExpandMoreProps) => {
    const { ...other } = props
    return <IconButton {...other} />
})(({ theme, expand }) => ({
    transform: !expand ? 'rotate(0deg)' : 'rotate(180deg)',
    marginLeft: 'auto',
    transition: theme.transitions.create('transform', {
        duration: theme.transitions.duration.shortest
    })
}))

interface CardProps {
    title: string;
    children: React.ReactNode;
}

const StatsCard = (props: CardProps) => {
    const { title, children } = props
    const [expanded, setExpanded] = useState(true)

    const handleExpandClick = () => {
        setExpanded(!expanded)
    }

    return <Card sx={{ minWidth: remCalc(240) }} raised>
        <CardHeader
            action={
                <ExpandMore
                    sx={{
                        padding: '5px',
                        marginRight: '5px'
                    }}
                    expand={expanded}
                    onClick={handleExpandClick}
                    aria-expanded={expanded}
                    aria-label="show more"
                >
                    <ExpandMoreIcon sx={{
                        width: 20,
                        height: 20
                    }} />
                </ExpandMore>
            }
            title={title}
        />
        <Collapse in={expanded} timeout="auto" unmountOnExit>
            <CardContent>
                {children}
            </CardContent>
        </Collapse>
    </Card>
}

export default StatsCard
