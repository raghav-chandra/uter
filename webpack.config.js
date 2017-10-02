'use strict';
 var path = require('path');

 var webpack = require('webpack');
     
 module.exports = {
     entry: './js/app.js',
     output: {
         path: path.resolve(__dirname, 'build'),
         filename: 'app.bundle.js'
     },
     module: {
         loaders: [
             {
                 test: /\.jsx$/,
                 loaders: ['babel-loader?presets[]=react,presets[]=es2015'],
		exclude: /node_modules/,
//                 query: {
//
//                     presets: ['es2015', 'react']
//                 }
             }
,
            {
                test: /\.html$/,
                loader: "file?name=[name].[ext]"
            }
         ]
     },
     stats: {
         colors: true
     },
     devtool: 'source-map'
 };
