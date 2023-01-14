const fetchTest = async () => {
    const url = 'http://localhost:8080/api/test/all'
    return fetch(url, {
        method: 'get'
    }).then((res) => {
        return res.json()
    })
}

export { fetchTest }
