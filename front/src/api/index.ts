import { RefillType } from 'types'

const fetchBrokers = async () => {
    const url = 'http://localhost:8080/api/broker/all'
    return fetch(url, {
        method: 'get'
    }).then((res) => {
        return res.json()
    })
}

const fetchCurrencies = async () => {
    const url = 'http://localhost:8080/api/currency/all'
    return fetch(url, {
        method: 'get'
    }).then((res) => {
        return res.json()
    })
}

const fetchMarkets = async () => {
    const url = 'http://localhost:8080/api/market/all'
    return fetch(url, {
        method: 'get'
    }).then((res) => {
        return res.json()
    })
}

const fetchTickers = async () => {
    const url = 'http://localhost:8080/api/ticker/all'
    return fetch(url, {
        method: 'get'
    }).then((res) => {
        return res.json()
    })
}

const fetchBrokerStats = async (brokerId: number) => {
    const url = `http://localhost:8080/api/broker/stats?brokerId=${brokerId}`
    return fetch(url, {
        method: 'get'
    }).then((res) => {
        return res.json()
    })
}

const fetchMoneyState = async () => {
    const url = 'http://localhost:8080/api/cash/state'
    return fetch(url, {
        method: 'get'
    }).then((res) => {
        return res.json()
    })
}

const postRefill = async (body: RefillType) => {
    const url = 'http://localhost:8080/api/cash/refill'
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
    postRefill
}
