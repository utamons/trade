// noinspection TypeScriptValidateTypes

import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import Dialog from '@mui/material/Dialog'
import { remCalc } from '../../utils/utils'
import Button from '../button'
import React, { useCallback, useState } from 'react'
import { ButtonContainerStyled } from '../../styles/style'
import { SelectChangeEvent } from '@mui/material/Select'
import { Box, styled } from '@mui/material'
import TextField from '@mui/material/TextField'
import { ExchangeDialogProps, ItemType, MarketType, PositionOpenType, TickerType } from 'types'
import Typography from '@mui/material/Typography'
import Select from '../select'

const ContainerStyled = styled(Box)(() => ({
    display: 'flex',
    flexFlow: 'column',
    fontSize: remCalc(12),
    justifyContent: 'space-between',
    padding: remCalc(8),
    gap: remCalc(20)
}))

const SelectBoxStyled = styled(Box)(() => ({
    display: 'flex',
    flexFlow: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    width: remCalc(160)
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

export default ({ onCancel, isOpen, currentBroker, markets, tickers, open, evaluate }: OpenDialogProps) => {
    const [marketId, setMarketId] = useState(''+markets[0].id)
    const [tickerId, setTickerId] = useState(''+tickers[0].id)

    const validate = (): boolean => {
        return false
    }

    const handleOpen = useCallback(() => {
        if (validate())
            return
        console.log('open')
    }, [])

    const handleMarketSelector = useCallback((event: SelectChangeEvent<unknown>) => {
        setMarketId(event.target.value as string)
    }, [])

    const handleTickerSelector = useCallback((event: SelectChangeEvent<unknown>) => {
        setTickerId(event.target.value as string)
    }, [])

    return <Dialog
        open={isOpen}
    >
        <DialogContent>
            <ContainerStyled>
                <SelectBoxStyled>
                    <Typography variant="caption">Market:</Typography>
                    <Select
                        items={markets}
                        value={marketId}
                        onChange={handleMarketSelector}
                        variant="medium"
                    />
                </SelectBoxStyled>
                <SelectBoxStyled>
                    <Typography variant="caption">Ticker:</Typography>
                    <Select
                        items={tickers}
                        value={tickerId}
                        onChange={handleTickerSelector}
                        variant="medium"
                    />
                </SelectBoxStyled>
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
