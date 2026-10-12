'use strict'
const appConfig = require('./app.config')

module.exports = {
  NODE_ENV: '"production"',
  API_BASE_URL: `"${appConfig.api.production}"`
}
