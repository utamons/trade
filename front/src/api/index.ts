import {
    EvalRequest,
    EvalToFitRequest,
    ExchangeType,
    PageRequest,
    PositionCloseType,
    PositionOpenType,
    RefillType
} from 'types'
import config from '../../config/config.json'
import { TimePeriod } from '../utils/constants'

const baseUrl = config.baseUrl

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
    const url = `${baseUrl}/stats/broker?brokerId=${brokerId}`
    return fetch(url, {
        method: 'get'
    }).then((res) => {
        return res.json()
    })
}

const fetchMoneyState = async () => {
    const url = `${baseUrl}/stats/state`
    return fetch(url, {
        method: 'get'
    }).then((res) => {
        return res.json()
    })
}

const fetchStats = async (timePeriod: TimePeriod, currencyId: number | undefined, brokerId: number | undefined) => {
    let params = '?timePeriod=' + timePeriod
    if (currencyId) {
        params += `&currencyId=${currencyId}`
    }
    if (brokerId) {
        params += `&brokerId=${brokerId}`
    }
    const url = `${baseUrl}/stats${params}`
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

const postCorrection = async (body: RefillType) => {
    const url = `${baseUrl}/cash/correction`
    return fetch(url, {
        method: 'post',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
    }).catch((err) => {
        throw err
    })
}

const postExchange = async (body: ExchangeType) => {
    const url = `${baseUrl}/cash/exchange`
    const res = await fetch(url, {
        method: 'post',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
    })
    if (!res.ok) {
        const body = await res.json()
        return Promise.reject(body.message)
    }
    return Promise.resolve()
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

const postOpen = async (body: PositionOpenType) => {
    const url = `${baseUrl}/log/open`
    return fetch(url, {
        method: 'post',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
    }).then()
}

const postEval = async (body: EvalRequest) => {
    const url = `${baseUrl}/cash/eval`
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

const postEvalToFit = async (body: EvalToFitRequest) => {
    const url = `${baseUrl}/cash/evaltofit`
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

const postClose = async (body: PositionCloseType) => {
    const url = `${baseUrl}/log/close`
    return fetch(url, {
        method: 'post',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
    }).then()
}

export {
    fetchStats,
    fetchBrokers,
    fetchCurrencies,
    fetchMarkets,
    fetchTickers,
    fetchBrokerStats,
    fetchMoneyState,
    postRefill,
    postCorrection,
    postExchange,
    postLogPage,
    postOpen,
    postEval,
    postEvalToFit,
    postClose
}
