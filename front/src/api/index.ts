const fetchBrokers = async () => {
    const url = 'http://localhost:8080/api/broker/all'
    return fetch(url, {
        method: 'get'
    }).then((res) => {
        return res.json()
    })
}

export { fetchBrokers }
