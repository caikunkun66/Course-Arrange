'use strict'
const merge = require('webpack-merge')
const prodEnv = require('./prod.env')
const appConfig = require('./app.config')

module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  API_BASE_URL: `"${appConfig.api.dev}"`
})
