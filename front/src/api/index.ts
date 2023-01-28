import { ExchangeType, PageRequest, RefillType } from 'types'

const baseUrl = 'http://localhost:8080/api'

const fetchBrokers = async () => {
    const url = `${baseUrl}/broker/all`
    return fetch(url, {
        method: 'get'
    }).then((res) => {
        return res.json()
    })
}

const fetchCurrencies = async () => {
    const url = `${baseUrl}/currency/all`
    return fetch(url, {
        method: 'get'
    }).then((res) => {
        return res.json()
    })
}

const fetchMarkets = async () => {
    const url = `${baseUrl}/market/all`
    return fetch(url, {
        method: 'get'
    }).then((res) => {
        return res.json()
    })
}

const fetchTickers = async () => {
    const url = `${baseUrl}/ticker/all`
    return fetch(url, {
        method: 'get'
    }).then((res) => {
        return res.json()
    })
}

const fetchBrokerStats = async (brokerId: number) => {
    const url = `${baseUrl}/broker/stats?brokerId=${brokerId}`
    return fetch(url, {
        method: 'get'
    }).then((res) => {
        return res.json()
    })
}

const fetchMoneyState = async () => {
    const url = `${baseUrl}/cash/state`
    return fetch(url, {
        method: 'get'
    }).then((res) => {
        return res.json()
    })
}

const postRefill = async (body: RefillType) => {
    const url = `${baseUrl}/cash/refill`
    return fetch(url, {
        method: 'post',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
    }).then((res) => {
        return res.json()
    })
}

const postExchange = async (body: ExchangeType) => {
    const url = `${baseUrl}/cash/exchange`
    return fetch(url, {
        method: 'post',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
    }).then((res) => {
        return res.json()
    })
}

const postLogPage = async (body: PageRequest) => {
    const url = `${baseUrl}/log/page`
    return fetch(url, {
        method: 'post',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
    }).then((res) => {
        return res.json()
    })
}

export {
    fetchBrokers,
    fetchCurrencies,
    fetchMarkets,
    fetchTickers,
    fetchBrokerStats,
    fetchMoneyState,
    postRefill,
    postExchange,
    postLogPage
}
