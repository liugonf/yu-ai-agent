<script setup>
import { ref } from 'vue'
import LoveChat from './components/LoveChat.vue'

function scrollToChat() {
  const el = document.getElementById('chat-area')
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}
</script>

<template>
  <div class="site">
    <!-- 顶部导航 -->
    <header class="topbar">
      <div class="topbar-inner">
        <div class="logo">
          <span class="logo-heart">💜</span>
          <span class="logo-text">AI 恋爱大师</span>
        </div>
        <nav class="nav">
          <a href="#hero">首页</a>
          <a href="#chat-area" @click="scrollToChat">AI 恋爱大师</a>
        </nav>
        <button class="btn-register">免费注册</button>
      </div>
    </header>

    <!-- Hero 横幅 -->
    <section class="hero" id="hero">
      <h1>遇见爱情，从一次对话开始</h1>
      <p>专业的 AI 恋爱顾问，陪你聊聊单身、恋爱与婚姻中的那些事</p>
      <div class="hero-actions">
        <button class="btn-primary" @click="scrollToChat">立即咨询</button>
      </div>
    </section>

    <!-- 恋爱大师聊天区 -->
    <main class="app" id="chat-area">
      <LoveChat />
    </main>

    <footer class="footer">
      AI 恋爱大师 · 仅供学习演示 · 数据由 AI 生成，请勿作为真实情感建议
    </footer>
  </div>
</template>

<style scoped>
.site { min-height: 100vh; display: flex; flex-direction: column; }

.topbar {
  position: sticky; top: 0; z-index: 20;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #efe9ff;
}
.topbar-inner { max-width: 1080px; margin: 0 auto; padding: 14px 20px; display: flex; align-items: center; gap: 28px; }
.logo { display: flex; align-items: center; gap: 8px; font-weight: 700; font-size: 20px; }
.logo-heart { font-size: 24px; }
.nav { display: flex; gap: 22px; margin-left: auto; }
.nav a { color: #4b5563; font-size: 15px; padding: 6px 2px; }
.btn-register { margin-left: auto; padding: 8px 20px; border: none; border-radius: 999px; background: linear-gradient(135deg, #8b5cf6, #6d28d9); color: #fff; font-size: 14px; cursor: pointer; }

.hero {
  text-align: center;
  padding: 72px 20px 64px;
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 50%, #e0e7ff 100%);
}
.hero h1 { font-size: 40px; margin: 0 0 14px; background: linear-gradient(135deg, #6d28d9, #8b5cf6); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.hero p { color: #6b7280; font-size: 17px; margin: 0 0 28px; }
.hero-actions { display: flex; gap: 16px; justify-content: center; }

.app { max-width: 900px; width: 100%; margin: -28px auto 48px; padding: 0 20px; }

.footer { margin-top: auto; text-align: center; padding: 22px; color: #9ca3af; font-size: 13px; }
</style>
