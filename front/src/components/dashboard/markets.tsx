import { Box, styled } from '@mui/material'
import { Loadable, remCalc } from '../../utils/utils'
import { useContext, useEffect, useState } from 'react'
import { TradeContext } from '../../trade-context'
import { MarketType } from 'types'

const ContainerStyled = styled(Box)(({ theme }) => ({
    display: 'flex',
    flexFlow: 'column wrap',
    color: theme.palette.text.primary,
    alignContent: 'flex-start',
    width: '50%',
    height: remCalc(131),
    padding: remCalc(10),
    gap: remCalc(20)
}))

const DateContainerStyled = styled(Box)(({ theme }) => ({
    alignItems: 'center',
    display: 'flex',
    color: theme.palette.text.primary,
    justifyContent: 'flex-start',
    gap: remCalc(10)
}))

interface DateElemProps {
    name: string,
    offset?: number
}

const timeWithOffset = (date: Date, offset: number | undefined) => {
    const options: Intl.DateTimeFormatOptions = {
        hour12: false,
        hour: '2-digit',
        minute: '2-digit'
    }
    if (offset) {
        const utc = date.getTime() + (date.getTimezoneOffset() * 60000);
        const newDate = new Date(utc + (3600000 * offset));
        return newDate.toLocaleString('en-us', options);
    }
    return date.toLocaleTimeString();
}

const DateElem = ({ name, offset }: DateElemProps) => {
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

const getMarketDates = (markets: MarketType[] | undefined) => {
    console.log('markets', markets)
    if (!markets)
        return <></>
    return <>{markets.map((market: MarketType) => (
        <DateElem key={market.id} name={market.name} offset={market.timezone}/>
    ))}</>
}

export default () => {
    const { all } = useContext(TradeContext)
    if (!all)
        return <></>
    const { isLoading, markets } = all

    return <Loadable isLoading={isLoading}>
        <ContainerStyled>
            {getMarketDates(markets)}
        </ContainerStyled>
    </Loadable>
}
