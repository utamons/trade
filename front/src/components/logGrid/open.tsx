// noinspection TypeScriptValidateTypes

import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import Dialog from '@mui/material/Dialog'
import { currencySymbol, remCalc } from '../../utils/utils'
import Button from '../button'
import React, { useCallback, useState } from 'react'
import { ButtonContainerStyled, FieldBox, FieldName } from '../../styles/style'
import { SelectChangeEvent } from '@mui/material/Select'
import { Box, styled } from '@mui/material'
import dayjs, { Dayjs } from 'dayjs'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker'
import TextField from '@mui/material/TextField'
import { ItemType, MarketType, PositionOpenType, TickerType } from 'types'
import Select from '../select'
import NumberInput from '../numberInput'

const ContainerStyled = styled(Box)(() => ({
    display: 'flex',
    justifyContent: 'flex-start',
    flexFlow: 'column wrap',
    fontSize: remCalc(14),
    fontFamily: 'sans',
    width: remCalc(900),
    height: remCalc(500)
}))

interface OpenDialogProps {
    currentBroker: ItemType,
    markets: MarketType [],
    tickers: TickerType [],
    evaluate: boolean,
    isOpen: boolean,
    open: (open: PositionOpenType) => void,
    onCancel: () => void
}

const positions = [
    { id: 0, name: 'long' },
    { id: 1, name: 'short' }
]

export const FieldValue = styled(Box)(() => ({
    display: 'flex',
    alignContent: 'flex-start',
    alignItems: 'center',
    width: remCalc(200)
}))

const BasicDateTimePicker = () => {
    const [value, setValue] = React.useState<Dayjs | null>(dayjs(new Date()))

    return (
        <LocalizationProvider dateAdapter={AdapterDayjs}>
            <DateTimePicker
                renderInput={(props) => <TextField {...props} />}
                value={value}
                inputFormat="YYYY-MM-DD HH:MM"
                onChange={(newValue) => {
                    setValue(newValue)
                }}
            />
        </LocalizationProvider>
    )
}

export default ({ onCancel, isOpen, currentBroker, markets, tickers, open, evaluate }: OpenDialogProps) => {
    const [marketId, setMarketId] = useState('' + markets[0].id)
    const [tickerId, setTickerId] = useState('' + tickers[0].id)
    const [currentTicker, setCurrentTicker] = useState<TickerType|undefined>(tickers[0])
    const [positionId, setPositionId] = useState('' + positions[0].id)
    const [priceError, setPriceError] = useState(false)
    const [itemsError, setItemsError] = useState(false)
    const [price, setPrice] = useState(0)
    const [items, setItems] = useState(0)

    const validate = (): boolean => {
        return false
    }

    const handleOpen = useCallback(() => {
        if (validate())
            return
        console.log('open')
    }, [])

    const handlePositionSelector = useCallback((event: SelectChangeEvent<unknown>) => {
        setPositionId(event.target.value as string)
    }, [])

    const handleMarketSelector = useCallback((event: SelectChangeEvent<unknown>) => {
        const id = Number(event.target.value)
        setMarketId(event.target.value as string)
        const ticker = tickers.find((t) => t.id == id)
        setCurrentTicker(ticker)
    }, [])

    const handleTickerSelector = useCallback((event: SelectChangeEvent<unknown>) => {
        setTickerId(event.target.value as string)

    }, [])


    return <Dialog
        maxWidth={false}
        open={isOpen}
    >
        <DialogContent>
            <ContainerStyled>
                <FieldBox>
                    <FieldName>Date:</FieldName>
                    <FieldValue>
                        <BasicDateTimePicker />
                    </FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Position:</FieldName>
                    <FieldValue>
                        <Select
                            items={positions}
                            value={positionId}
                            onChange={handlePositionSelector}
                            variant="medium"/>
                    </FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Broker:</FieldName>
                    <FieldValue>{currentBroker.name}</FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Market:</FieldName>
                    <FieldValue>
                        <Select
                            items={markets}
                            value={marketId}
                            onChange={handleMarketSelector}
                            variant="medium"
                        />
                    </FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Ticker:</FieldName>
                    <FieldValue>
                        <Select
                            items={tickers}
                            value={tickerId}
                            onChange={handleTickerSelector}
                            variant="medium"
                        />
                    </FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Price: {`(${currentTicker?(currentTicker.currency.name):'???'})`}</FieldName>
                    <FieldValue>
                        <NumberInput onChange={setPrice} onError={setPriceError}/>
                    </FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Items:</FieldName>
                    <FieldValue>
                        <NumberInput onChange={setItems} onError={setItemsError}/>
                    </FieldValue>
                </FieldBox>
            </ContainerStyled>
        </DialogContent>

        <DialogActions sx={{ justifyContent: 'center' }}>
            <ButtonContainerStyled>
                <Button style={{ minWidth: remCalc(101) }} text="Open" onClick={handleOpen}/>
                <Button style={{ minWidth: remCalc(101) }} text="Cancel" onClick={onCancel}/>
            </ButtonContainerStyled>
        </DialogActions>
    </Dialog>
}
