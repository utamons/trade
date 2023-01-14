import React, { createContext, useEffect, useMemo, useState } from 'react'
import { onetimeQueryOptions } from '@kkr/lpportal-react-shared'
import { useQuery } from 'react-query'
import { DocumentType, FetchFilter, ListResult, QuickFilter } from 'types'
import { fetchDocuments } from '../api'
import getQuickFilters from './quickfilters'
import { mediumDate } from '@kkr/lpportal-frontend-shared'

export const EffectiveDate = 'EffectiveDate'
// only EffectiveDate desc by default
const getDefaultDesc = (field: string) => (EffectiveDate === field)

type DocumentList = {
    documents: DocumentType[],
    quickFilters: QuickFilter[],
    hasMore: boolean
    loadMore: () => void
    isLoading: boolean
    sort: string
    desc: boolean
    sortBy: (field: string) => void
    filterBy: (filter: FetchFilter) => void
    unread: boolean
    toggleUnread: () => void
}

const getPageParams = (documents: DocumentType[], pageSize: number, lastResult: ListResult | null) => {
    const displayCount = documents.length
    let skip = 0

    if (documents.length === 0) {
        // very first time, double size
        return { size: pageSize * 2, skip }
    }

    if (!lastResult) {
        // reset, 0 to current size + 1-page caching
        return { size: displayCount + pageSize, skip }
    }
    // normal: based on previous result
    skip = lastResult.BatchOffset + lastResult.BatchSize

    return { size: pageSize, skip }
}

const processResult = (result: ListResult): ListResult => {
    result.Documents.forEach((document) => {
        document.DocumentDate = mediumDate(document.DocumentDate)
        document.EffectiveDate = mediumDate(document.EffectiveDate)
        document.ExpirationDate = mediumDate(document.ExpirationDate)
    })
    return result
}

const getPage = async (lastResult: ListResult | null,
    skip: number,
    size: number,
    sort: string,
    desc: boolean,
    unread: boolean,
    filter: FetchFilter| undefined) => {
    if (lastResult && skip >= lastResult.TotalCount) {
        // no more to retrieve
        lastResult.Documents = []
        return lastResult
    } else {
        return await fetchDocuments(skip, size, sort, desc, unread, filter)
            .then(processResult)
    }
}

const useDocumentList = (pageSize = 1): DocumentList | null => {
    const [documents, setDocuments] = useState<DocumentType[]>([])
    const [cache, setCache] = useState<DocumentType[]>([])
    const [pageNumber, setPageNumber] = useState(0)
    const [sort, setSort] = useState<string>('EffectiveDate')
    const [desc, setDesc] = useState(true)
    const [unread, setUnread] = useState(false)
    const [lastResult, setLastResult] = useState<ListResult | null>(null)
    const [filter, setFilter] = useState<FetchFilter|undefined>()
    let quickFilters: QuickFilter[] = []

    const { data, isLoading } = useQuery([sort, desc, pageNumber],
        async () => {
            const { size, skip } = getPageParams(documents, pageSize, lastResult)
            const result = await getPage(lastResult, skip, size, sort, desc, unread, filter)

            return { result }
        }, onetimeQueryOptions()
    )

    if (data) {
        quickFilters = getQuickFilters(data.result)
    }

    useEffect(() => {
        if (!data) return
        const { result } = data

        if (documents.length === 0) {
            // very first time, double size preloaded
            setDocuments(result.Documents.slice(0, pageSize))
            setCache(result.Documents.slice(pageSize))
            setLastResult(result)
            return
        }
        if (lastResult === null) {
            // reset to current document length
            setDocuments(result.Documents.slice(0, documents.length))
            setCache(result.Documents.slice(documents.length))
            setLastResult(result)
            return
        }
        setCache(() => result.Documents)
        setLastResult(() => result)
    }, [data])

    useEffect(() => {
        // use cache
        setDocuments((last) => last.concat(cache))
        setCache(() => [])
    }, [pageNumber])

    if (lastResult === null) return null

    const sortBy = (field = sort) => {
        if (field === sort) {
            // toggle descending
            setDesc(last => !last)
        } else {
            // switching field starts from default desc setting
            setSort(() => field)
            setDesc(() => getDefaultDesc(field))
        }
        setCache(() => [])
        setLastResult(() => null)
    }

    const filterBy = (filter: FetchFilter) => {
        setFilter(filter)
    }

    const toggleUnread = () => {
        setUnread(last => !last)
        setLastResult(null)
    }

    const hasMore = lastResult.TotalCount > documents.length
    const loadMore = () => setPageNumber((last) => last + 1)

    console.log('quickFilters to return - ', quickFilters.length)

    return {
        documents,
        quickFilters,
        hasMore,
        loadMore,
        isLoading,
        sort, desc, sortBy, filterBy,
        unread, toggleUnread
    }

}

export const DocListContext = createContext({
    all: null as DocumentList | null
})

export const DocListProvider = ({ children }: { children: JSX.Element }) => {
    const all = useDocumentList(20)
    const value = useMemo(() => ({ all }), [all])

    return (
        <DocListContext.Provider value={value}>
            {children}
        </DocListContext.Provider>
    )
}

// for tests
export { getPageParams, getPage }
