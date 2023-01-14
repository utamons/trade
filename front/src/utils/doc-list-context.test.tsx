import React from 'react'
import { getPageParams } from './doc-list-context'
import { DocumentType, ListResult } from 'types'
import data from '../../test/mock/documents.json'

describe('DocListContext', () => {
    describe('getPageParams', () => {
        it('getPageParams should return params for very first time', () => {
            const docs: DocumentType[] = []
            const pageSize = 10
            const lastResult = null

            const { size, skip } = getPageParams(docs, pageSize, lastResult)
            expect(size).toEqual(pageSize*2)
            expect(skip).toEqual(0)
        })
        it('getPageParams should return params for no lastResult', () => {
            const docs: DocumentType[] = data.Documents
            const pageSize = 10
            const lastResult = null

            const { size, skip } = getPageParams(docs, pageSize, lastResult)
            expect(size).toEqual(docs.length + pageSize)
            expect(skip).toEqual(0)
        })
        it('getPageParams should return params for normal', () => {
            const docs: DocumentType[] = data.Documents
            const pageSize = 10
            const lastResult = data

            const { size, skip } = getPageParams(docs, pageSize, lastResult)
            expect(size).toEqual(pageSize)
            expect(skip).toEqual(data.BatchOffset + data.BatchSize)
        })
    })
})
