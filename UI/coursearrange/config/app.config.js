'use strict'

// 统一配置文件 - 所有环境配置都在这里管理
module.exports = {
  // 服务器配置
  server: {
    host: '127.0.0.1',  // 开发服务器主机地址
    port: 8081              // 开发服务器端口
  },

  // API配置
  api: {
    dev: 'http://127.0.0.1:8008',      // 开发环境API地址
    production: 'http://127.0.0.1:8008' // 生产环境API地址
  }
}

