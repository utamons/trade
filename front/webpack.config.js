const path = require('path')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const webpackDashboard = require('webpack-dashboard/plugin')
const { CleanWebpackPlugin } = require('clean-webpack-plugin')

module.exports = {
    mode: 'development',
    entry: './src/index.tsx',
    output: {
        path: path.resolve(__dirname, './dist'),
        publicPath: `/`,
        chunkFilename: 'assets/[name].[contenthash].js',
        filename: 'assets/[name].[contenthash].js'
    },
    devtool: 'inline-source-map',
    devServer: {
        port: 4200,
        static: {
            directory: path.join(__dirname, './dist')
        },
        historyApiFallback:{
            index: `http://localhost:4200/index.html`
        }
    },
    module: {
        rules: [
            {
                test: /\.jsx?$/,
                exclude: /node_modules/,
                loader: 'esbuild-loader',
                options: {
                    loader: 'jsx',
                    target: 'esnext'
                }
            },
            {
                test: /\.css$/,
                use: ['css-loader']
            },
            {
                test: /\.tsx?$/,
                exclude: /node_modules/,
                loader: 'esbuild-loader',
                options: {
                    loader: 'tsx',
                    target: 'esnext'
                }
            }
        ]
    },
    resolve: { extensions: ['.tsx', '.ts', '.js'] },
    plugins: [
        new HtmlWebpackPlugin(
            Object.assign(
                {},
                {
                    inject: true,
                    favicon: 'src/styles/assets/favicon.ico',
                    templateContent: `
                        <html lang="en">                       
                             <head>
                                <meta charset="utf-8">
                                <title>Trade</title>
                                <meta name="viewport" content="width=device-width,initial-scale=1">
                             </head>                        
                             <body style="margin: 0; background-color: aliceblue">
                                <div id="root"></div>
                             </body>
                        </html>`
                }
            )
        ),
        new webpackDashboard(),
        new CleanWebpackPlugin()
    ]
}
