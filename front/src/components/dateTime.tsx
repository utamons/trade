import { Box, styled } from '@mui/material'
import { Loadable, remCalc } from '../utils/utils'
import { useContext, useEffect, useState } from 'react'
import { TradeContext } from '../utils/trade-context'
import { MarketType } from 'types'

const ContainerStyled = styled(Box)(({theme}) => ({
    alignItems: 'top',
    display: 'flex',
    color: theme.palette.primary.main,
    justifyContent: 'left'
}))

const DateContainerStyled = styled(Box)(({theme}) => ({
    alignItems: 'center',
    display: 'flex',
    color: theme.palette.primary.main,
    justifyContent: 'flex-start',
    gap: remCalc(10),
    padding: remCalc(10)
}))

interface DateElemProps {
    name: string,
    offset?: number
}

const timeWithOffset = (date: Date, offset: number| undefined) => {
    if (offset) {
        const utc = date.getTime() + (date.getTimezoneOffset() * 60000);
        const newDate = new Date(utc + (3600000*offset));
        return newDate.toLocaleTimeString();
    }
    return date.toLocaleTimeString();
}

const DateElem = ({name, offset}: DateElemProps) => {
    const [time, setTime] = useState(new Date());

    useEffect(() => {
        const intervalId = setInterval(() => {
            setTime(new Date());
        }, 1000);

        return () => clearInterval(intervalId);
    }, []);

    return <DateContainerStyled>
        <Box>{name}:</Box>
        <Box>{timeWithOffset(time, offset)}</Box>
    </DateContainerStyled>
}

const getMarketDates = (markets: MarketType[]) => {
    console.log('markets',markets)
    if (!markets)
        return <></>
    return <>{markets.map((market: MarketType) => (
        <DateElem key={market.id} name={market.name} offset={market.timezone}/>
        ))}</>
}

export default () => {
    const { all } = useContext(TradeContext)
    const { isLoading, markets } = all

    return <ContainerStyled>
        <Loadable isLoading={isLoading}>
            {getMarketDates(markets)}
        </Loadable>
    </ContainerStyled>
}
