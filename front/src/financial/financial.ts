import { TickerType } from 'types'

const getFees = (brokerName: string, ticker: TickerType, items: number, price: number): number => {
    if (items == 0 || price == 0)
        return 0
    if (brokerName == 'FreedomFN') {
        const currency = ticker.currency.name
        if (currency == 'KZT') {
            return (items * price) / 100 * 0.085
        } else {
            const sum = items * price
            const fixed = items < 100 ? 1.2 : items * 0.012
            return sum / 100 * 0.5 + fixed
        }
    }
    return 0
}

const getRisk = (deposit: number, items: number, price: number, stopLoss: number) => {
    const sumOpen = items * price
    const losses = sumOpen - items * stopLoss
    return losses / deposit * 100
}

export { getFees, getRisk }
