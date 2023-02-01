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
import { BrokerStatsType, ItemType, MarketType, PositionOpenType, TickerType } from 'types'
import Select from '../select'
import NumberInput from '../numberInput'
import { postEval } from '../../api'

const ContainerStyled = styled(Box)(() => ({
    display: 'flex',
    justifyContent: 'flex-start',
    flexFlow: 'column wrap',
    fontSize: remCalc(14),
    fontFamily: 'sans',
    width: remCalc(700),
    height: remCalc(400)
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

interface DatePickerProps {
    onChange: (date: Date) => void
}

const BasicDateTimePicker = ( { onChange }: DatePickerProps ) => {
    const [value, setValue] = React.useState<Dayjs | null>(dayjs(new Date()))

    const handleChange = useCallback((value) => {
        setValue(value)
        onChange(value.Date)
    }, [])

    return (
        <LocalizationProvider dateAdapter={AdapterDayjs}>
            <DateTimePicker
                renderInput={(props) => <TextField {...props} />}
                value={value}
                inputFormat="YYYY-MM-DD HH:MM"
                onChange={handleChange}
            />
        </LocalizationProvider>
    )
}

interface Eval {
    fees: number,
    risk: number
}


export default ({ onCancel, isOpen, currentBroker, markets, tickers, open, evaluate }: OpenDialogProps) => {
    const [marketId, setMarketId] = useState('' + markets[0].id)
    const [tickerId, setTickerId] = useState('' + tickers[0].id)
    const [currentTicker, setCurrentTicker] = useState<TickerType|undefined>(tickers[0])
    const [positionId, setPositionId] = useState('' + positions[0].id)
    const [priceError, setPriceError] = useState(false)
    const [itemsError, setItemsError] = useState(false)
    const [stopLossError, setStopLossError] = useState(false)
    const [takeProfitError, setTakeProfitError] = useState(false)
    const [outcomeExpError, setOutcomeExpError] = useState(false)
    const [price, setPrice] = useState(0)
    const [items, setItems] = useState(0)
    const [stopLoss, setStopLoss] = useState(0)
    const [takeProfit, setTakeProfit] = useState(0)
    const [outcomeExp, setOutcomeExp] = useState(0)
    const [risk, setRisk] = useState<number|undefined>(undefined)
    const [fees, setFees] = useState<number|undefined>(undefined)
    const [note, setNote] = useState('')
    const [date, setDate] = useState(new Date())
    const [depoUSD, setDepoUSD] = useState(0)

    const validate = (): boolean => {
        return false
    }

    console.log('price', price)

    const handleOpen = useCallback(async () => {
        if (validate())
            return
        if (fees == undefined || risk == undefined) {
            const ev: Eval = await postEval({
                brokerId: currentBroker.id,
                tickerId: Number(tickerId),
                priceOpen: price,
                items,
                stopLoss,
                date: date.toISOString()
            })
            setFees(ev.fees)
            setRisk(ev.risk)
        }
    }, [tickerId, price, items, stopLoss, date])

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


    const getFees = () => {

        return 1
    }

    const getRisk = () => {
        return 1
    }

    const noteChangeHandler = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
        setNote(event.target.value)
    }, [])

    const dateChangeHandler = useCallback((newDate: Date) => {
        setDate(newDate)
    }, [])

    const buttonName = fees == undefined || risk == undefined ? 'Evaluate' : 'Open'

    return <Dialog
        maxWidth={false}
        open={isOpen}
    >
        <DialogContent>
            <ContainerStyled>
                <FieldBox>
                    <FieldName>Date:</FieldName>
                    <FieldValue>
                        <BasicDateTimePicker onChange={dateChangeHandler} />
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
                <FieldBox>
                    <FieldName>Stop Loss:</FieldName>
                    <FieldValue>
                        <NumberInput onChange={setStopLoss} onError={setStopLossError}/>
                    </FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Take Profit:</FieldName>
                    <FieldValue>
                        <NumberInput onChange={setTakeProfit} onError={setTakeProfitError}/>
                    </FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Out. Exp.:</FieldName>
                    <FieldValue>
                        <NumberInput onChange={setOutcomeExp} onError={setOutcomeExpError}/>
                    </FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Fees:</FieldName>
                    <FieldValue>
                        {fees}
                    </FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Risk:</FieldName>
                    <FieldValue>
                        {risk}
                    </FieldValue>
                </FieldBox>
                <FieldBox>
                    <FieldName>Note:</FieldName>
                    <FieldValue>
                        <TextField
                            id="outlined-textarea"
                            multiline
                            onChange={noteChangeHandler}
                        />
                    </FieldValue>
                </FieldBox>
            </ContainerStyled>
        </DialogContent>

        <DialogActions sx={{ justifyContent: 'center' }}>
            <ButtonContainerStyled>
                <Button style={{ minWidth: remCalc(101) }} text={buttonName} onClick={handleOpen}/>
                <Button style={{ minWidth: remCalc(101) }} text="Cancel" onClick={onCancel}/>
            </ButtonContainerStyled>
        </DialogActions>
    </Dialog>
}
