module.exports = {
    preset: 'ts-jest',
    transform: {
        '^.+\\.(ts|tsx)?$': 'ts-jest',
        '^.+\\.(js|jsx)$': 'babel-jest'
    },
    moduleNameMapper:{
        '\\.(css|less)$': '<rootDir>/test/style-mock.js',
        '\\.(gif|png|jpg|ttf|eot|svg|woff|woff2)$': '<rootDir>/test/file-mock.js'
    },
    testEnvironment: 'jest-environment-jsdom',
    collectCoverage: true,
    collectCoverageFrom: ['src/**/*.{js,ts,jsx,tsx}', '!src/api/**/*'],
    coveragePathIgnorePatterns: [
        'node_modules',
        'test',
        '.d.ts',
        '.mock.ts'
    ],
    setupFilesAfterEnv: ['<rootDir>/test/setup-tests.tsx']
}
