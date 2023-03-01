// noinspection TypeScriptValidateTypes

import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import Dialog from '@mui/material/Dialog'
import { breakEvenColor, RED, remCalc, riskColor, roundTo2 } from '../../utils/utils'
import Button from '../button'
import React, { useCallback, useState } from 'react'
import { ButtonContainerStyled, FieldName } from '../../styles/style'
import { SelectChangeEvent } from '@mui/material/Select'
import { Box, Grid, styled } from '@mui/material'
import dayjs, { Dayjs } from 'dayjs'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import TextField from '@mui/material/TextField'
import { ItemType, MarketType, PositionOpenType, TickerType } from 'types'
import Select from '../select'
import NumberInput from '../numberInput'
import { postEval, postEvalToFit } from '../../api'
import Switch from '@mui/material/Switch'
import { DesktopDateTimePicker } from '@mui/x-date-pickers'
import { useTheme } from '@emotion/react'
import CircularProgress from '@mui/material/CircularProgress'

interface OpenDialogProps {
    currentBroker: ItemType,
    markets: MarketType [],
    tickers: TickerType [],
    isOpen: boolean,
    open: (open: PositionOpenType) => void,
    onClose: () => void
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

const FieldBox = styled(Box)(() => ({
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    width: remCalc(300),
    height: remCalc(50)
}))

const SwitchBox = styled(Box)(() => ({
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    width: remCalc(150),
    height: remCalc(50)
}))

const NoteBox = styled(Box)(() => ({
    width: '100%',
    marginTop: remCalc(10)
}))

const RedSwitch = styled(Switch)(() => ({
    '& .MuiSwitch-switchBase.Mui-checked': {
        color: RED,
        '&:hover': {
            backgroundColor: 'rgba(204, 51, 0, 0.2)'
        }
    },
    '& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track': {
        backgroundColor: RED
    }
}))

interface DatePickerProps {
    onChange: (date: Date) => void
}

export const TextFieldStyled = styled(TextField)(() => ({
    '.MuiOutlinedInput-root': {
        borderRadius: remCalc(2),
        fontSize: remCalc(14),
        maxHeight: remCalc(38),
        maxWidth: remCalc(170)
    },
    '.MuiSvgIcon-root ': {
        fontSize: remCalc(18)
    }
}))

const BasicDateTimePicker = ({ onChange }: DatePickerProps) => {
    const [value, setValue] = React.useState<Dayjs | null>(dayjs(new Date()))

    const handleChange = useCallback((value: Dayjs | null) => {
        if (value) {
            setValue(value)
            onChange(value.toDate())
        }
    }, [])

    return (
        <LocalizationProvider dateAdapter={AdapterDayjs}>
            <DesktopDateTimePicker
                renderInput={(props) => <TextFieldStyled {...props} />}
                value={value}
                inputFormat="YYYY-MM-DD HH:mm"
                onChange={handleChange}
                onAccept={handleChange}
            />
        </LocalizationProvider>
    )
}

interface Eval {
    fees: number,
    risk: number,
    breakEven: number,
    outcomeExp: number,
    takeProfit: number
}

interface EvalToFit {
    fees: number,
    risk: number,
    breakEven: number,
    outcomeExp: number,
    takeProfit: number,
    stopLoss: number,
    items: number
}

export default ({ onClose, isOpen, currentBroker, markets, tickers, open }: OpenDialogProps) => {
    const [marketId, setMarketId] = useState('' + markets[0].id)
    const [tickerId, setTickerId] = useState('' + tickers[0].id)
    const [currentTicker, setCurrentTicker] = useState<TickerType | undefined>(tickers[0])
    const [positionId, setPositionId] = useState('' + positions[0].id)
    const [priceError, setPriceError] = useState(false)
    const [itemsError, setItemsError] = useState(false)
    const [stopLossError, setStopLossError] = useState(false)
    const [takeProfitError, setTakeProfitError] = useState(false)
    const [outcomeExpError, setOutcomeExpError] = useState(false)
    const [price, setPrice] = useState<number | undefined>(undefined)
    const [items, setItems] = useState<number | undefined>(undefined)
    const [stopLoss, setStopLoss] = useState<number | undefined>(undefined)
    const [takeProfit, setTakeProfit] = useState<number | undefined>(undefined)
    const [outcomeExp, setOutcomeExp] = useState<number | undefined>(undefined)
    const [risk, setRisk] = useState<number | undefined>(undefined)
    const [fees, setFees] = useState<number | undefined>(undefined)
    const [breakEven, setBreakEven] = useState<number | undefined>(undefined)
    const [note, setNote] = useState('')
    const [date, setDate] = useState(new Date())
    const [evaluate, setEvaluate] = useState(true)
    const [isLoading, setLoading] = useState(false)

    const theme = useTheme()
    // noinspection TypeScriptUnresolvedVariable
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    const defaultColor = theme.palette.text.primary

    const validate = (): boolean => {
        if (priceError || itemsError || stopLossError || takeProfitError || outcomeExpError)
            return true
        return price == undefined || items == undefined || stopLoss == undefined

    }

    const breakEvenPercentage = () => {
        if (breakEven && price)
            return Math.abs(breakEven / (price / 100) - 100.0)
        else
            return 0
    }

    const breakEvenPercentageStr = () => {
        if (breakEven && price)
            return `(${roundTo2(Math.abs(breakEven / (price / 100) - 100))}%)`
        else
            return ''
    }
    const handleEvalToFit = useCallback(async () => {
        if (evaluate && price) {
            setLoading(true)
            const ev: EvalToFit = await postEvalToFit({
                brokerId: currentBroker.id,
                tickerId: Number(tickerId),
                priceOpen: price,
                items,
                stopLoss,
                date: date.toISOString(),
                short: positionId == '1'
            })
            setLoading(false)
            setItems(ev.items)
            setStopLoss(ev.stopLoss)
            setFees(ev.fees)
            setRisk(ev.risk)
            setBreakEven(ev.breakEven)
            setTakeProfit(ev.takeProfit)
            setOutcomeExp(ev.outcomeExp)
            return
        }
    }, [
        evaluate,
        price,
        items,
        stopLoss
    ])

    const handleOpen = useCallback(async () => {
        if (validate()) {
            console.error('validation failed')
            return
        }
        if (evaluate && price && items && stopLoss) {
            console.log('evaluation')
            const ev: Eval = await postEval({
                brokerId: currentBroker.id,
                tickerId: Number(tickerId),
                priceOpen: price,
                items,
                stopLoss,
                date: date.toISOString(),
                short: positionId == '1'
            })
            setFees(ev.fees)
            setRisk(ev.risk)
            setBreakEven(ev.breakEven)
            setTakeProfit(ev.takeProfit)
            setOutcomeExp(ev.outcomeExp)
            return
        }
        if (price && items && stopLoss && risk && fees) {
            open({
                position: positionId == '0' ? 'long' : 'short',
                dateOpen: date.toISOString(),
                brokerId: currentBroker.id,
                marketId: Number(marketId),
                tickerId: Number(tickerId),
                itemNumber: items,
                priceOpen: price,
                stopLoss: stopLoss,
                takeProfit: takeProfit,
                outcomeExpected: outcomeExp,
                risk: risk,
                breakEven: breakEven,
                fees: fees,
                note: note
            })
            onClose()
        }
    }, [
        evaluate,
        positionId,
        marketId,
        tickerId,
        price,
        items,
        stopLoss,
        date,
        risk,
        fees,
        breakEven,
        note,
        takeProfit,
        outcomeExp])

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

    const noteChangeHandler = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
        setNote(event.target.value)
    }, [])

    const dateChangeHandler = useCallback((newDate: Date) => {
        console.log('New date:', newDate)
        setDate(newDate)
    }, [])

    const handleSwitch = useCallback((event: React.SyntheticEvent, checked: boolean) => {
        setEvaluate(!checked)
    }, [])

    const priceChangeHandler = useCallback((newPrice: number) => {
        setPrice(newPrice)
    }, [])

    const priceErrorHandler = useCallback((error: boolean) => {
        setPriceError(error)
    }, [])

    const itemsChangeHandler = useCallback((newItems: number) => {
        setItems(newItems)
    }, [])

    const itemsErrorHandler = useCallback((error: boolean) => {
        setItemsError(error)
    }, [])

    const stopLossChangeHandler = useCallback((newStopLoss: number) => {
        setStopLoss(newStopLoss)
    }, [])

    const stopLossErrorHandler = useCallback((error: boolean) => {
        setStopLossError(error)
    }, [])

    const takeProfitChangeHandler = useCallback((newTakeProfit: number) => {
        setTakeProfit(newTakeProfit)
    }, [])

    const takeProfitErrorHandler = useCallback((error: boolean) => {
        setTakeProfitError(error)
    }, [])

    const outcomeExpChangeHandler = useCallback((newOutcomeExp: number) => {
        setOutcomeExp(newOutcomeExp)
    }, [])

    const outcomeExpErrorHandler = useCallback((error: boolean) => {
        setOutcomeExpError(error)
    }, [])

    return <Dialog
        maxWidth={false}
        open={isOpen}
    >
        <DialogContent sx={{ fontSize: remCalc(12), fontFamily: 'sans' }}>
            <Grid sx={{ width: remCalc(700) }} container columns={2}>
                <Grid item xs={2}>
                    <SwitchBox>
                        <FieldName>Open:</FieldName>
                        <RedSwitch
                            size="small"
                            checked={!evaluate}
                            onChange={handleSwitch}
                        />
                    </SwitchBox>
                </Grid>
                <Grid item xs={1}>
                    <Grid container columns={1}>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Date:</FieldName>
                                <FieldValue>
                                    <BasicDateTimePicker onChange={dateChangeHandler}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
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
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Broker:</FieldName>
                                <FieldValue>{currentBroker.name}</FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Market:</FieldName>
                                <FieldValue>
                                    <Select
                                        items={markets}
                                        value={marketId}
                                        onChange={handleMarketSelector}
                                        variant="small"
                                    />
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Ticker:</FieldName>
                                <FieldValue>
                                    <Select
                                        items={tickers}
                                        value={tickerId}
                                        onChange={handleTickerSelector}
                                        variant="small"
                                    />
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>
                                    Price: {`(${currentTicker ? (currentTicker.currency.name) : '???'})`}
                                </FieldName>
                                <FieldValue>
                                    <NumberInput
                                        value={price}
                                        onChange={priceChangeHandler}
                                        onError={priceErrorHandler}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                    </Grid>
                </Grid>
                <Grid item xs={1}>
                    <Grid container columns={1}>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Items:</FieldName>
                                <FieldValue>
                                    <NumberInput
                                        value={items}
                                        onChange={itemsChangeHandler}
                                        onError={itemsErrorHandler}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Stop Loss:</FieldName>
                                <FieldValue>
                                    <NumberInput
                                        value={stopLoss}
                                        onChange={stopLossChangeHandler}
                                        onError={stopLossErrorHandler}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Take Profit:</FieldName>
                                <FieldValue>
                                    <NumberInput
                                        value={takeProfit}
                                        onChange={takeProfitChangeHandler}
                                        onError={takeProfitErrorHandler}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Out. Exp.:</FieldName>
                                <FieldValue>
                                    <NumberInput
                                        value={outcomeExp}
                                        onChange={outcomeExpChangeHandler}
                                        onError={outcomeExpErrorHandler}/>
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Fees:</FieldName>
                                <FieldValue>
                                    {fees}
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Risk:</FieldName>
                                <FieldValue sx={riskColor(risk, defaultColor)}>
                                    {risk} %
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                        <Grid item xs={1}>
                            <FieldBox>
                                <FieldName>Break even:</FieldName>
                                <FieldValue sx={breakEvenColor(breakEvenPercentage(), defaultColor)}>
                                    {breakEven} {breakEvenPercentageStr()}
                                </FieldValue>
                            </FieldBox>
                        </Grid>
                    </Grid>
                </Grid>
                <Grid item xs={2}>
                    <NoteBox>
                        <TextField
                            id="outlined-textarea"
                            label="Note"
                            multiline
                            style={{ width: '100%', fontSize: remCalc(14) }}
                            onChange={noteChangeHandler}
                        />
                    </NoteBox>
                </Grid>
            </Grid>
        </DialogContent>

        <DialogActions sx={{ justifyContent: 'center' }}>
            <ButtonContainerStyled>
                {isLoading ? < CircularProgress size={20}/> :
                    <>{evaluate ? <Button style={{ minWidth: remCalc(101) }} text="Evaluate To Fit"
                                          onClick={handleEvalToFit}/> : <></>}
                        <Button style={{ minWidth: remCalc(101) }} text={evaluate ? 'Evaluate' : 'Open'}
                                onClick={handleOpen}/>
                        <Button style={{ minWidth: remCalc(101) }} text="Cancel" onClick={onClose}/> </>}
            </ButtonContainerStyled>
        </DialogActions>
    </Dialog>
}
