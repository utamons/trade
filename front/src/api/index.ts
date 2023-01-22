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

const fetchTickers= async () => {
    const url = 'http://localhost:8080/api/ticker/all'
    return fetch(url, {
        method: 'get'
    }).then((res) => {
        return res.json()
    })
}

export { fetchBrokers, fetchCurrencies, fetchMarkets, fetchTickers }
