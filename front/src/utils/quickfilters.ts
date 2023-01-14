import { ListResult, QuickFilter, StubType } from 'types'

const filterMatrix: QuickFilter[] = [
    {
        filter: {
            Id: 20001,
            Name: 'Capital Calls/Distributions'
        },
        children: [
            {
                Id: 20009,
                Name: 'Capital Call Notice'
            },
            {
                Id: 20010,
                Name: 'Distribution Notice'
            }
        ]
    },
    {
        filter: {
            Id: 20002,
            Name: 'KKR Internal'
        },
        children: [
            {
                Id: 20011,
                Name: 'Group Valuations'
            },
            {
                Id: 20012,
                Name: 'Investment Updates'
            },
            {
                Id: 20013,
                Name: 'Solicitations'
            },
            {
                Id: 20014,
                Name: 'KKR Shares'
            },
            {
                Id: 20015,
                Name: 'Profit Participation Award (DAW)'
            },
            {
                Id: 20016,
                Name: 'Tax Projections'
            },
            {
                Id: 20049,
                Name: 'Quarterly Reports'
            },
            {
                Id: 20050,
                Name: 'Quarterly Reports - Liquidated'
            }
        ]
    },
    {
        filter: {
            Id: 20003,
            Name: 'Other'
        },
        children: [
            {
                Id: 20017,
                Name: 'Other'
            },
            {
                Id: 20018,
                Name: 'Marketing Meetings'
            }
        ]
    },
    {
        filter: {
            Id: 20004,
            Name: 'Portfolio Company Documents'
        },
        children: [
            {
                Id: 20019,
                Name: 'Acquisition Announcements'
            },
            {
                Id: 20020,
                Name: 'Portfolio Company-Issued Financials and Reports'
            },
            {
                Id: 20021,
                Name: 'Quarterly Deal Summary'
            },
            {
                Id: 20041,
                Name: 'Transaction Summary'
            }
        ]
    },
    {
        filter: {
            Id: 20005,
            Name: 'Read Only Document Types'
        },
        children: [
            {
                Id: 20022,
                Name: 'Advisory Committee Documents'
            },
            {
                Id: 20023,
                Name: 'Underwriting Materials'
            }
        ]
    },
    {
        filter: {
            Id: 20006,
            Name: 'Regulatory & Legal'
        },
        children: [
            {
                Id: 20024,
                Name: 'Regulatory Disclosures'
            },
            {
                Id: 20025,
                Name: 'Organizational Documents'
            },
            {
                Id: 20026,
                Name: 'Compliance Certification'
            },
            {
                Id: 2027,
                Name: 'ERISA Certification'
            },
            {
                Id: 20052,
                Name: 'LPAC Invitations'
            }
        ]
    },
    {
        filter: {
            Id: 20007,
            Name: 'Reporting'
        },
        children: [
            {
                Id: 20056,
                Name: 'ILPA Portfolio Company Metrics Template'
            },
            {
                Id: 20042,
                Name: 'Historical Investor Reports'
            },
            {
                Id: 20043,
                Name: 'Valuations - Investor level'
            },
            {
                Id: 20028,
                Name: 'Capital Account Statements'
            },
            {
                Id: 20029,
                Name: 'Unused Capital Commitment'
            },
            {
                Id: 20030,
                Name: 'Financial Statements - Annual'
            },
            {
                Id: 20031,
                Name: 'Financial Statements -Quarterly'
            },
            {
                Id: 20032,
                Name: 'Monthly Reporting'
            },
            {
                Id: 20033,
                Name: 'Quarterly Letters & Presentation'
            },
            {
                Id: 20034,
                Name: 'ILPA Reporting Template'
            },
            {
                Id: 20035,
                Name: 'Transparency Report'
            },
            {
                Id: 20036,
                Name: 'Valuations - Fund level'
            }
        ]
    },
    {
        filter: {
            Id: 20008,
            Name: 'Tax Documents'
        },
        children: [
            {
                Id: 20037,
                Name: 'FATCA'
            },
            {
                Id: 20038,
                Name: 'K-1s/Tax Filings'
            },
            {
                Id: 20039,
                Name: 'Portfolio Company Tax Documents'
            },
            {
                Id: 20040,
                Name: 'Withholding Tax form'
            }
        ]
    },
    {
        filter: {
            Id: 20044,
            Name: 'Mailing Campaign Only'
        },
        children: [
            {
                Id: 20045,
                Name: 'Ad Hoc Mailing'
            }
        ]
    },
    {
        filter: {
            Id: 20047,
            Name: 'Reference Documents'
        },
        children: [
            {
                Id: 20048,
                Name: 'Driver Report'
            }
        ]
    },
    {
        filter: {
            Id: 20051,
            Name: 'Limited Partner Advisory Committee (Confidential)'
        },
        children: [
            {
                Id: 20053,
                Name: 'LPAC Meetings'
            },
            {
                Id: 20054,
                Name: 'LPAC Consents'
            }
        ]
    },
    {
        filter: {
            Id: 20057,
            Name: 'Direct Investing Outbound'
        },
        children: [
            {
                Id: 20058,
                Name: 'Direct Investing Data Template'
            }
        ]
    }
]

export default (data: ListResult): QuickFilter[] => {
    const result: QuickFilter[] = []
    const types: StubType[] = data.DocumentTypes || []
    const subTypes: StubType[] = data.DocumentSubTypes || []

    if (types) {
        types.forEach((type) => {
            const filter: QuickFilter = {
                filter: type,
                children: []
            }
            filterMatrix.forEach((f: QuickFilter) => {
                let children: StubType[] = []
                if (f.filter.Id === type.Id && f.children) {
                    children = f.children.filter((v: StubType) => {
                        return subTypes.find((s) => {
                            return s.Id === v.Id
                        })
                    })
                }
                filter.children.push.apply(filter.children, children)
            })
            result.push(filter)
        })
    }
    return result
}
