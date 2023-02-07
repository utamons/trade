// eslint-disable-next-line no-undef
module.exports = {
    'env': {
        'browser': true,
        'es2021': true
    },
    'extends': [
        'eslint:recommended',
        'plugin:react/recommended',
        'plugin:@typescript-eslint/recommended'
    ],
    'overrides': [
    ],
    'parser': '@typescript-eslint/parser',
    'parserOptions': {
        'ecmaVersion': 'latest',
        'sourceType': 'module'
    },
    'plugins': [
        'react',
        '@typescript-eslint'
    ],
    'ignorePatterns': ['*.js', '**/*.test.tsx', 'test'],
    'rules': {
        'prefer-spread': ['off'],
        'quotes': [
            'error',
            'single'
        ],
        'semi': [
            'error',
            'never'
        ],
        'object-curly-spacing': ['error', 'always'],
        'comma-spacing': 'error',
        'react/display-name': 'off',
        'comma-dangle': ['error', 'never'],
        'no-use-before-define': 'off'
    }
}
