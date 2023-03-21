import Select from '@mui/material/Select'
import React from 'react'
import MenuItem from '@mui/material/MenuItem'
import { ItemType, SelectorProps } from 'types'
import { remCalc } from '../../utils/utils'

export default ({ items, value, onChange, variant } : SelectorProps) => {
    let fontSize = remCalc(18)
    if (variant && variant == 'medium') {
        fontSize = remCalc(14)
    }
    if (variant && variant == 'small') {
        fontSize = remCalc(12)
    }

    const getItems = (itemz: ItemType[]) => {
        return itemz ?
            itemz.map(
                (item: ItemType) =>
                    <MenuItem sx={{ fontSize }} key={item.id}
                        value={item.id}>{item.name}
                    </MenuItem>) : <></>
    }

    return <Select
        sx={{ fontSize }}
        label="from"
        variant="standard"
        value={value}
        onChange={onChange}
    >
        {getItems(items)}
    </Select>
}
