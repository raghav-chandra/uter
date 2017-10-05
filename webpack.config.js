const path = require('path');

const HtmlWebpackPlugin = require('html-webpack-plugin');
const HtmlWebpackPluginConfig = new HtmlWebpackPlugin({
  template: './app/index.html',
  filename: 'index.html',
  inject: 'body'
})
module.exports = {
     entry: path.resolve(__dirname,'./app/main.js'),
     output: {
         path: path.resolve(__dirname,'./dist'),
         filename: 'app.bundle.js'
     },
     module: {
    	loaders: [
      		{ test: /\.(js|jsx)$/, loader: 'babel-loader', exclude: /node_modules/ }
    	]
     },

 plugins: [HtmlWebpackPluginConfig]
}
