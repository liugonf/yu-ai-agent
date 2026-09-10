# yu-ai-agent-frontend

AI 恋爱大师 + AI 超级智能体的前端项目（Vue 3 + Vite，SSE 实时输出）。

## 启动

```bash
npm install
npm run dev
```

浏览器访问 http://localhost:5173

## 说明

- 后端接口前缀：开发环境为 `http://localhost:8124/api`（`src/api/index.js` 可改）
- 恋爱大师走 `/ai/love_app/chat/sse`（SSE 流式）
- 超级智能体走 `/ai/manus/chat`（SSE 分步输出）
- 生产环境通过 Nginx 把 `/api` 反向代理到后端，避免跨域

## 打包 / 部署

```bash
npm run build        # 产物在 dist/
```

配合 `Dockerfile` 与 `nginx.conf` 可打包为镜像部署到 Serverless 平台。
