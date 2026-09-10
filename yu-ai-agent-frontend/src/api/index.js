// 后端接口地址前缀：开发环境直连本地后端，生产环境走 Nginx 反向代理 /api
export const API_BASE_URL = import.meta.env.PROD
  ? '/api'
  : 'http://localhost:8124/api'
