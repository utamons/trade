
const MAX_DEPOSIT_PC= 25 // Maximum volume allowed for a trade in percentage of the deposit
const MAX_RISK_REWARD_PC = 33.33 // Minimum risk reward ratio in percentage
const MAX_RISK_PC = 0.5 // Maximum risk allowed in percentage of the deposit

export {
    MAX_DEPOSIT_PC,
    MAX_RISK_REWARD_PC,
    MAX_RISK_PC
}

export enum TimePeriod {
    ALL_TIME = 'ALL_TIME',
    WEEK_TO_DATE = 'WEEK_TO_DATE',
    MONTH_TO_DATE = 'MONTH_TO_DATE',
    YEAR_TO_DATE = 'YEAR_TO_DATE',
    LAST_WEEK = 'LAST_WEEK',
    LAST_MONTH = 'LAST_MONTH',
    LAST_YEAR = 'LAST_YEAR',
    QUARTER_TO_DATE = 'QUARTER_TO_DATE',
    LAST_QUARTER = 'LAST_QUARTER'
}
