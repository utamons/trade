import React, { Dispatch, useContext, useEffect, useState } from 'react'
import { Box, Grid, styled } from '@mui/material'
import { remCalc } from '../../utils/utils'
import { TradeContext } from '../../trade-context'
import Select from '../tools/select'
import { getFieldValue, useForm } from '../dialogs/dialogUtils'
import { fetchStats } from '../../api'
import { FormAction, FormActionPayload, FormState, ItemType, StatsType } from 'types'
import { TimePeriod } from '../../utils/constants'
import StatsCard from './card'
import StatsFieldBox from './statsFieldBox'

const FilterContainer = styled(Box)(({ theme }) => ({
    display: 'flex',
    flexFlow: 'row',
    alignItems: 'center',
    color: theme.palette.text.primary,
    backgroundColor: theme.palette.background.default,
    justifyContent: 'flex-start',
    gap: remCalc(20),
    borderColor: theme.palette.text.primary,
    padding: `${remCalc(15)} ${remCalc(20)} 0 ${remCalc(20)}`
}))

const MainContainer = styled(Grid)(({ theme }) => ({
    color: theme.palette.text.primary,
    backgroundColor: theme.palette.background.default,
    padding: `${remCalc(15)} ${remCalc(20)} 0 ${remCalc(20)}`
}))

type DateRange = ItemType & {
    enum: TimePeriod
}

const dateRangeItems: DateRange[] = [
    {
        id: 0,
        name: 'Week to date',
        enum: TimePeriod.WEEK_TO_DATE
    },
    {
        id: 1,
        name: 'Last week',
        enum: TimePeriod.LAST_WEEK
    },
    {
        id: 2,
        name: 'Month to date',
        enum: TimePeriod.MONTH_TO_DATE
    },
    {
        id: 3,
        name: 'Last month',
        enum: TimePeriod.LAST_MONTH
    },
    {
        id: 4,
        name: 'Quarter to date',
        enum: TimePeriod.QUARTER_TO_DATE
    },
    {
        id: 5,
        name: 'Last quarter',
        enum: TimePeriod.LAST_QUARTER
    },
    {
        id: 6,
        name: 'Year to date',
        enum: TimePeriod.YEAR_TO_DATE
    },
    {
        id: 7,
        name: 'Last year',
        enum: TimePeriod.LAST_YEAR
    },
    {
        id: 8,
        name: 'All time',
        enum: TimePeriod.ALL_TIME
    }
]
const initFormState = (formState: FormState, dispatch: Dispatch<FormAction>) => {
    if (formState.isInitialized)
        return

    const payload: FormActionPayload = {
        valuesNumeric: [
            {
                name: 'brokerId',
                valid: true,
                value: 1
            },
            {
                name: 'currencyId',
                valid: true,
                value: 1
            },
            {
                name: 'dateRange',
                valid: true,
                value: 0
            }
        ]
    }

    dispatch({ type: 'init', payload })
}

const StatsContainer = styled(Box)(() => ({
    padding: remCalc(10)
}))


const Stats = () => {
    const { brokers, currencies } = useContext(TradeContext)
    const { formState, dispatch } = useForm()
    const [stats, setStats] = useState<StatsType>()

    initFormState(formState, dispatch)

    const brokerId = '' + getFieldValue('brokerId', formState) as unknown as number
    const currencyId = '' + getFieldValue('currencyId', formState) as unknown as number
    const dateRange = '' + getFieldValue('dateRange', formState) as unknown as number

    const brokerItems: ItemType[] = []
    const currencyItems: ItemType[] = []

    if (brokers) {
        brokerItems.push({
            id: 0,
            name: 'All brokers'
        })
        for (const broker of brokers) {
            brokerItems.push({
                id: broker.id,
                name: broker.name
            })
        }
    }

    if (currencies) {
        currencyItems.push({
            id: 0,
            name: 'All currencies'
        })
        for (const currency of currencies) {
            currencyItems.push({
                id: currency.id,
                name: currency.name
            })
        }
    }

    useEffect(() => {
        const ti = dateRangeItems.find((elem: DateRange) => {
            return elem.id == dateRange
        }) ?? dateRangeItems[0]
        fetchStats(ti.enum, brokerId, currencyId).then((data: StatsType) => {
            setStats(data)
        }).catch((err: any) => {
            console.log('fetchStats error', err)
        })
    }, [brokerId, currencyId, dateRange])

    return <><FilterContainer>
        <Select
            label="Broker"
            items={brokerItems}
            value={brokerId as unknown as string}
            name={'brokerId'}
            variant={'medium'}
            dispatch={dispatch}/>
        <Select
            label="Currency"
            items={currencyItems}
            value={currencyId as unknown as string}
            name={'currencyId'}
            variant={'medium'}
            dispatch={dispatch}/>
        <Select
            label="Date range"
            items={dateRangeItems}
            value={dateRange as unknown as string}
            name={'dateRange'}
            variant={'medium'}
            dispatch={dispatch}/>
    </FilterContainer>
        <Box sx={{ width: '80%' }}>
            <MainContainer container columns={{ xs: 8 }} spacing={{ xs: 3 }}>
                <Grid item xs={2}>
                    <StatsCard title={'Common data'}>
                        <StatsContainer>
                            <StatsFieldBox label={'Total trades'} value={stats?.trades}/>
                            <StatsFieldBox label={'Days with trade'} value={stats?.dayWithTradesDayRatio}/>
                            <StatsFieldBox label={'Trades / day (max)'} value={stats?.tradesPerDayMax}/>
                            <StatsFieldBox label={'Trades / day (avg)'} value={stats?.tradesPerDayAvg}/>
                        </StatsContainer>
                    </StatsCard>
                </Grid>
                <Grid item xs={2}>
                    <StatsCard title={'Volume'}>
                        <StatsContainer>
                            <StatsFieldBox label={'Volume / trade (max)'} value={stats?.volumePerTradeMax}/>
                            <StatsFieldBox label={'Volume / trade (avg)'} value={stats?.volumePerTradeAvg}/>
                            <StatsFieldBox label={'Volume / day (max)'} value={stats?.volumePerDayMax}/>
                            <StatsFieldBox label={'Volume / day (avg)'} value={stats?.volumePerDayAvg}/>
                            <StatsFieldBox label={'Volume'} value={stats?.volume}/>
                        </StatsContainer>
                    </StatsCard>
                </Grid>
                <Grid item xs={2}>
                    <StatsCard title={'Quality'}>
                        <StatsContainer>
                            <StatsFieldBox variant={'pc'} label={'R/R (avg)'} value={stats?.riskRewardRatioAvg}/>
                            <StatsFieldBox variant={'pc'} label={'R/R (max)'} value={stats?.riskRewardRatioMax}/>
                            <StatsFieldBox variant={'pc'} label={'Win rate'} value={stats?.lossPerTradeMax}/>
                            <StatsFieldBox variant={'pc'} label={'Capital turnover'} value={stats?.capitalTurnover}/>
                            <StatsFieldBox label={'Slippage (avg)'} value={stats?.lossPerTradeMax}/>
                            <StatsFieldBox label={'Take delta (avg)'} value={stats?.takeDeltaAvg}/>
                            <StatsFieldBox label={'Stop delta (avg)'} value={stats?.stopDeltaAvg}/>
                        </StatsContainer>
                    </StatsCard>
                </Grid>
                <Grid item xs={2}>
                    <StatsCard title={'Profit'}>
                        <StatsContainer>
                            <StatsFieldBox label={'Profit / trade (avg)'} value={stats?.profitPerTradeAvg}/>
                            <StatsFieldBox label={'Profit / day (avg)'} value={stats?.profitPerDayAvg}/>
                            <StatsFieldBox label={'Profit / day (max)'} value={stats?.profitPerDayMax}/>
                            <StatsFieldBox label={'Profit partials (avg)'} value={stats?.profitPartialsAvg}/>
                            <StatsFieldBox label={'Profit singles (avg)'} value={stats?.profitSinglesAvg}/>
                            <StatsFieldBox label={'Profit'} value={stats?.profit}/>
                            <StatsFieldBox variant={'pc'} label={'Profit / volume'} value={stats?.profitVolumePc}/>
                            <StatsFieldBox variant={'pc'} label={'Profit / capital'} value={stats?.profitCapitalPc}/>
                        </StatsContainer>
                    </StatsCard>
                </Grid>
                <Grid item xs={2}>
                    <StatsCard title={'Commissions'}>
                        <StatsContainer>
                            <StatsFieldBox label={'Comm. / trade (max)'} value={stats?.commissionsPerTradeAvg}/>
                            <StatsFieldBox label={'Commissions'} value={stats?.commissions}/>
                        </StatsContainer>
                    </StatsCard>
                </Grid>
                <Grid item xs={2}>
                    <StatsCard title={'Loss'}>
                        <StatsContainer>
                            <StatsFieldBox label={'Losses'} value={stats?.loss}/>
                            <StatsFieldBox label={'Loss / trade (avg)'} value={stats?.lossPerTradeAvg}/>
                            <StatsFieldBox label={'Loss / trade (max)'} value={stats?.lossPerTradeMax}/>
                        </StatsContainer>
                    </StatsCard>
                </Grid>
                <Grid item xs={2}>
                    <StatsCard title={'Money'}>
                        <StatsContainer>
                            <StatsFieldBox label={'Capital'} value={stats?.capital}/>
                            <StatsFieldBox label={'Refills'} value={stats?.refills}/>
                            <StatsFieldBox label={'Withdrawals'} value={stats?.withdrawals}/>
                            <StatsFieldBox label={'Capital change'} value={stats?.capitalChange}/>
                        </StatsContainer>
                    </StatsCard>
                </Grid>
            </MainContainer>
        </Box>
    </>
}

export default Stats
