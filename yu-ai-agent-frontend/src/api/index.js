// 统一使用同源 /api：
// - 开发环境由 vite.config.js 中的 server.proxy 转发到后端 8124
// - 生产环境由 Nginx 反向代理 /api 到后端
export const API_BASE_URL = '/api'
