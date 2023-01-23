const path = require('path')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const MiniCssExtractPlugin = require('mini-css-extract-plugin')
const TerserPlugin = require('terser-webpack-plugin')
const {CleanWebpackPlugin} = require('clean-webpack-plugin')

module.exports = {
    mode: 'production',
    entry: './src/index.tsx',
    output: {
        path: path.resolve(__dirname, './dist'),
        publicPath: `/`,
        chunkFilename: 'assets/[name].[contenthash].js',
        filename: 'assets/[name].[contenthash].js',
        sourceMapFilename: "assets/[name].[contenthash].map"
    },
    optimization: {
        minimize: true,
        minimizer: [
            new MiniCssExtractPlugin({
                filename: 'assets/[name].[contenthash].css'
            }),
            new TerserPlugin({
                parallel: true,
                minify: TerserPlugin.uglifyJsMinify
            })
        ]
    },
    devtool: 'hidden-source-map',
    module: {
        rules: [
            {
                test: /\.jsx?$/,
                exclude: /node_modules/,
                use: 'babel-loader'
            },
            {
                test: /\.css$/,
                use: [MiniCssExtractPlugin.loader, 'css-loader']
            },
            {
                test: /\.tsx?$/,
                exclude: /node_modules/,
                use: 'ts-loader'
            }
        ]
    },
    resolve: {extensions: ['.tsx', '.ts', '.js']},
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
                             <body style="margin: 0">
                                <div id="root"></div>
                             </body>
                        </html>`
                },
                {
                    minify: {
                        removeComments: true,
                        collapseWhitespace: true,
                        removeRedundantAttributes: true,
                        useShortDoctype: true,
                        removeEmptyAttributes: true,
                        removeStyleLinkTypeAttributes: true,
                        keepClosingSlash: true,
                        minifyJS: true,
                        minifyCSS: true,
                        minifyURLs: true
                    }
                }
            )
        ),
        new CleanWebpackPlugin()
    ]
}
