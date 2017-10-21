var ExtractTextPlugin = require("extract-text-webpack-plugin");
const path = require('path');

const HtmlWebpackPlugin = require('html-webpack-plugin');
const HtmlWebpackPluginConfig = new HtmlWebpackPlugin({
  template: './app/index.html',
  filename: 'index.html',
  inject:false
})
module.exports = {
     entry: {	'app.bundle.js':path.resolve(__dirname,'./app/main.js'),
     		'app.bundle.css':path.resolve(__dirname,'./app/scss/main.scss')
     		},
     output: {
	path: path.resolve(__dirname,'./dist'),
        filename: '[name]'
     },
     module: {
        loaders: [
                { test: /\.(js|jsx)$/, loader: 'babel-loader', exclude: /node_modules/ },
                { test: /\.(png|woff|woff2|eot|ttf|svg)$/, loader: 'url-loader?limit=100000' },
                { test: /\.(css)$/, loader: ExtractTextPlugin.extract("css-loader", "css-loader") },
                { test: /\.(sass|scss)$/, loader: ExtractTextPlugin.extract("css-loader", "css-loader!sass-loader") }
        ]
     },

 plugins: [HtmlWebpackPluginConfig,new ExtractTextPlugin("[name]")]
}
