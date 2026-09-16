<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import LoveChat from './components/LoveChat.vue'
import Features from './components/Features.vue'
import HowItWorks from './components/HowItWorks.vue'
import Testimonials from './components/Testimonials.vue'
import FAQ from './components/FAQ.vue'

const mobileMenuOpen = ref(false)

function scrollToChat() {
  const el = document.getElementById('chat-area')
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}

function scrollToFeatures() {
  const el = document.getElementById('features')
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}

// 滚动揭示动画:为带 .reveal 的元素在进入视口时加 .is-visible
// fail-open:默认可见;仅 html.js 存在时才隐藏,且 JS 失败/IO 不支持时全部直接显示
let io = null
let scrollPending = false
let pollTimer = null

function revealInView() {
  const els = document.querySelectorAll('.reveal:not(.is-visible)')
  const trigger = window.innerHeight * 0.85
  els.forEach((el) => {
    const r = el.getBoundingClientRect()
    // 元素顶部已进入视口上 85% 区域,或已滚出到视口上方(已"看过"),都揭示。
    // 这样对瞬时跳跃式滚动也稳健:元素只要在某一刻进入过触发线即被揭示。
    if (r.top < trigger) {
      el.classList.add('is-visible')
    }
  })
}

function onScroll() {
  if (scrollPending) return
  scrollPending = true
  requestAnimationFrame(() => {
    revealInView()
    scrollPending = false
  })
}

onMounted(() => {
  // 双 rAF:确保子组件挂载 + 首帧绘制完成后再采集 .reveal,规避时序竞争
  requestAnimationFrame(() => requestAnimationFrame(() => {
    const els = document.querySelectorAll('.reveal')
    if (!('IntersectionObserver' in window)) {
      els.forEach((el) => el.classList.add('is-visible'))
      return
    }
    io = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-visible')
            io.unobserve(entry.target)
          }
        })
      },
      { threshold: 0.12, rootMargin: '0px 0px -40px 0px' }
    )
    els.forEach((el) => io.observe(el))
    // 兜底一:捕获 IO 可能漏掉的元素,滚动/调整尺寸时补触发
    window.addEventListener('scroll', onScroll, { passive: true })
    window.addEventListener('resize', onScroll)
    // 兜底二:轮询,完全不依赖 scroll 事件是否触发(前 ~12 秒持续检查)
    revealInView()
    pollTimer = setInterval(() => {
      revealInView()
      // 全部揭示后停止轮询
      if (document.querySelectorAll('.reveal:not(.is-visible)').length === 0) {
        clearInterval(pollTimer)
        pollTimer = null
      }
    }, 200)
    setTimeout(() => { if (pollTimer) { clearInterval(pollTimer); pollTimer = null } }, 12000)
  }))
})
onUnmounted(() => {
  io && io.disconnect()
  window.removeEventListener('scroll', onScroll)
  window.removeEventListener('resize', onScroll)
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<template>
  <div class="site">
    <!-- 装饰:右上角手绘墨水爱心,做旧信件标识 -->
    <div class="paper-mark" aria-hidden="true">
      <svg width="42" height="42" viewBox="0 0 100 100">
        <path d="M50 88 C20 65 10 45 10 30 C10 18 20 10 32 10 C40 10 46 14 50 22 C54 14 60 10 68 10 C80 10 90 18 90 30 C90 45 80 65 50 88 Z"
          fill="none" stroke="#7a1f2b" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
      <span class="paper-mark-text">No.01 · 私人信件</span>
    </div>

    <!-- 顶部导航 -->
    <header class="topbar">
      <div class="topbar-inner container">
        <div class="logo">
          <span class="logo-mark">
            <!-- 内联手绘墨水爱心,替代 emoji -->
            <svg width="22" height="22" viewBox="0 0 100 100">
              <path d="M50 88 C20 65 10 45 10 30 C10 18 20 10 32 10 C40 10 46 14 50 22 C54 14 60 10 68 10 C80 10 90 18 90 30 C90 45 80 65 50 88 Z"
                fill="#7a1f2b" />
            </svg>
          </span>
          <span class="logo-text">AI 超级智能体<em>·情书集</em></span>
        </div>
        <nav class="nav" :class="{ open: mobileMenuOpen }">
          <a href="#hero" @click="mobileMenuOpen = false">首页</a>
          <a href="#features" @click="mobileMenuOpen = false">能力</a>
          <a href="#steps" @click="mobileMenuOpen = false">用法</a>
          <a href="#chat-area" @click="scrollToChat; mobileMenuOpen = false">对话</a>
        </nav>
        <button class="btn-register" @click="scrollToChat">开始对话</button>
        <button class="menu-toggle" @click="mobileMenuOpen = !mobileMenuOpen" aria-label="菜单">
          <span></span><span></span><span></span>
        </button>
      </div>
    </header>

    <!-- Hero 区块:不对称杂志感,左 7 列大字 + 右 5 列倾斜信件 -->
    <section class="hero" id="hero">
      <div class="hero-inner container">
        <div class="hero-copy">
          <div class="hero-eyebrow reveal">
            <span class="eyebrow-line"></span>
            Issue · 流式对话 · 越聊越懂你
          </div>
          <h1 class="hero-title reveal reveal-delay-1">
            有人愿意<br />
            <em>听你说</em>
            <span class="hero-title-tail">每一种心情。</span>
          </h1>
          <p class="hero-sub reveal reveal-delay-2">
            不只是 AI。它记得你昨天的心事,接住你今天的疲惫,
            像一封永不寄出的手写信,24 小时都在。
          </p>
          <div class="hero-actions reveal reveal-delay-3">
            <button class="btn-primary" @click="scrollToChat">
              <span>开始对话</span>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14M13 6l6 6-6 6"/></svg>
            </button>
            <button class="btn-ghost" @click="scrollToFeatures">看看怎么聊</button>
          </div>

          <!-- 信任数据:报刊式三栏 -->
          <div class="hero-stats reveal reveal-delay-4">
            <div class="stat">
              <div class="stat-num">10万<em>+</em></div>
              <div class="stat-label">用户陪伴</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat">
              <div class="stat-num">4.9<em>★</em></div>
              <div class="stat-label">用户评分</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat">
              <div class="stat-num">24/7</div>
              <div class="stat-label">全天在线</div>
            </div>
          </div>
        </div>

        <!-- 右侧:倾斜信件式聊天预览,带邮戳与做旧边线 -->
        <div class="hero-visual reveal reveal-delay-2">
          <div class="mock-stamp">SSE<br />·LIVE·</div>
          <div class="mock-chat">
            <div class="mock-head">
              <span class="mock-avatar">
                <svg width="16" height="16" viewBox="0 0 100 100"><path d="M50 88 C20 65 10 45 10 30 C10 18 20 10 32 10 C40 10 46 14 50 22 C54 14 60 10 68 10 C80 10 90 18 90 30 C90 45 80 65 50 88 Z" fill="currentColor"/></svg>
              </span>
              <span class="mock-name">AI 智能体<span class="mock-status">· 在线</span></span>
            </div>
            <div class="mock-body">
              <div class="mock-row ai"><span class="mock-bubble">你今天过得怎么样?想说就告诉我。</span></div>
              <div class="mock-row user"><span class="mock-bubble">有点累,工作压力有点大。</span></div>
              <div class="mock-row ai"><span class="mock-bubble">辛苦了。先深呼吸一下,我在陪你,不着急。</span></div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 功能特性 -->
    <Features />

    <!-- 使用流程 -->
    <HowItWorks />

    <!-- 超级智能体区域 -->
    <section class="chat-section" id="chat-area">
      <div class="container">
        <div class="section-head reveal">
          <span class="section-eyebrow">· 立即对话 ·</span>
          <h2 class="section-title">和你的 <span class="gradient-text">AI 智能体</span> 聊聊</h2>
          <p class="section-subtitle">无需注册,直接开始。试试下面的话题,或输入你想说的任何话。</p>
        </div>
        <div class="chat-wrap reveal">
          <LoveChat />
        </div>
      </div>
    </section>

    <!-- 用户口碑 -->
    <Testimonials />

    <!-- 常见问题 -->
    <FAQ />

    <!-- 结尾 CTA:牛血红实色 + 纸纹 -->
    <section class="cta-section">
      <div class="container">
        <div class="cta-card reveal">
          <div class="cta-eyebrow">· P.S. ·</div>
          <h2 class="cta-title">现在就开始,<em>遇见</em> 懂你的 AI</h2>
          <p class="cta-sub">免费体验,无需注册。你的情绪、你的故事,有人愿意听。</p>
          <button class="btn-primary cta-btn" @click="scrollToChat">
            <span>开始对话</span>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14M13 6l6 6-6 6"/></svg>
          </button>
        </div>
      </div>
    </section>

    <footer class="footer">
      <div class="container footer-inner">
        <div class="footer-brand">
          <div class="logo">
            <span class="logo-mark">
              <svg width="22" height="22" viewBox="0 0 100 100"><path d="M50 88 C20 65 10 45 10 30 C10 18 20 10 32 10 C40 10 46 14 50 22 C54 14 60 10 68 10 C80 10 90 18 90 30 C90 45 80 65 50 88 Z" fill="#f4ebd9"/></svg>
            </span>
            <span class="logo-text footer-logo-text">AI 超级智能体<em>·情书集</em></span>
          </div>
          <p class="footer-desc">用 AI 技术、以爱之名,为你打造最佳情感伙伴。<br />—— 像一封永不寄出的手写信。</p>
        </div>
        <div class="footer-cols">
          <div class="footer-col">
            <h4>产品</h4>
            <a href="#features">能力特性</a>
            <a href="#steps">使用流程</a>
            <a href="#chat-area">开始对话</a>
          </div>
          <div class="footer-col">
            <h4>支持</h4>
            <a href="#faq">常见问题</a>
            <a href="#chat-area">在线反馈</a>
          </div>
        </div>
      </div>
      <div class="footer-bottom container">
        <span>© {{ new Date().getFullYear() }} AI 超级智能体 · 你恋爱大师</span>
        <span class="footer-tag">用 AI 技术、以爱之名、为你打造最佳情感伙伴</span>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.site { min-height: 100vh; display: flex; flex-direction: column; position: relative; overflow-x: hidden; }

/* 右上手绘墨水标记 */
.paper-mark {
  position: absolute;
  top: 18px;
  right: 24px;
  z-index: 60;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  opacity: 0.78;
  pointer-events: none;
  transform: rotate(-6deg);
}
.paper-mark-text {
  font-family: var(--font-display);
  font-style: italic;
  font-size: 10px;
  letter-spacing: 1.2px;
  color: var(--primary);
  white-space: nowrap;
}

/* ===== 顶部导航:暖纸底 + 牛血红线 ===== */
.topbar {
  position: sticky; top: 0; z-index: 50;
  background: rgba(244, 235, 217, 0.88);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid var(--border);
}
.topbar-inner { display: flex; align-items: center; gap: 32px; padding: 16px 28px; }
.logo { display: flex; align-items: baseline; gap: 10px; color: var(--ink); }
.logo-mark { display: inline-flex; align-items: center; justify-content: center; animation: floatY 4s ease-in-out infinite; }
.logo-text { font-family: var(--font-display); font-size: 20px; font-weight: 600; letter-spacing: -0.3px; }
.logo-text em { font-style: italic; font-weight: 400; color: var(--primary); margin-left: 4px; font-size: 14px; }

.nav { display: flex; gap: 30px; margin-left: auto; }
.nav a {
  color: var(--ink-soft);
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.3px;
  padding: 6px 2px;
  position: relative;
  transition: color 0.18s;
}
.nav a::after {
  content: '';
  position: absolute;
  left: 0; bottom: -2px;
  width: 100%; height: 1.5px;
  background: var(--primary);
  transform: scaleX(0);
  transform-origin: left center;
  transition: transform 0.28s cubic-bezier(0.2, 0.7, 0.2, 1);
}
.nav a:hover { color: var(--primary); }
.nav a:hover::after { transform: scaleX(1); }

.btn-register {
  margin-left: auto;
  padding: 10px 24px;
  border: 1.5px solid var(--primary);
  border-radius: var(--radius-pill);
  background: transparent;
  color: var(--primary);
  font-family: var(--font-body);
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.3px;
  cursor: pointer;
  transition: all 0.22s;
}
.btn-register:hover { background: var(--primary); color: var(--paper-light); transform: translateY(-1px); }

.menu-toggle { display: none; flex-direction: column; gap: 5px; background: none; border: none; cursor: pointer; padding: 6px; margin-left: auto; }
.menu-toggle span { width: 22px; height: 2px; background: var(--ink); border-radius: 2px; transition: 0.25s; }

/* ===== Hero:7/5 不对称杂志感 ===== */
.hero {
  position: relative;
  padding: 96px 0 120px;
  overflow: hidden;
  background:
    radial-gradient(ellipse at 80% 15%, rgba(200, 144, 44, 0.07) 0, transparent 50%),
    radial-gradient(ellipse at 15% 85%, rgba(122, 31, 43, 0.05) 0, transparent 55%);
}
/* 装饰:大号衬线斜体"亲爱的"作背景 */
.hero::before {
  content: 'Dear.';
  position: absolute;
  top: 8%;
  left: -2%;
  font-family: var(--font-display);
  font-style: italic;
  font-weight: 300;
  font-size: clamp(180px, 28vw, 320px);
  color: var(--primary);
  opacity: 0.08;
  pointer-events: none;
  line-height: 0.9;
  z-index: 0;
}
.hero-inner {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 64px;
  align-items: center;
}
.hero-copy { max-width: 620px; }

.hero-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  font-family: var(--font-body);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 3px;
  text-transform: uppercase;
  color: var(--primary);
  margin-bottom: 28px;
}
.eyebrow-line { width: 32px; height: 1.5px; background: var(--primary); display: inline-block; }

.hero-title {
  font-family: var(--font-display);
  font-size: clamp(44px, 7vw, 86px);
  line-height: 1.02;
  margin: 0 0 28px;
  font-weight: 500;
  letter-spacing: -1.6px;
  color: var(--ink);
  font-variation-settings: "opsz" 144, "SOFT" 80;
}
.hero-title em {
  font-style: italic;
  font-weight: 400;
  color: var(--primary);
  position: relative;
  display: inline-block;
}
.hero-title em::after {
  content: '';
  position: absolute;
  left: 6%; right: 6%;
  bottom: 6%;
  height: 10px;
  background: var(--accent-soft);
  opacity: 0.5;
  z-index: -1;
  border-radius: 4px;
}
.hero-title-tail { font-weight: 300; }

.hero-sub {
  color: var(--ink-soft);
  font-family: var(--font-body);
  font-size: clamp(15px, 1.6vw, 17px);
  margin: 0 0 36px;
  line-height: 1.85;
  max-width: 480px;
}

.hero-actions { display: flex; gap: 14px; flex-wrap: wrap; }

/* 信任数据:三栏报刊式 */
.hero-stats {
  display: flex;
  align-items: stretch;
  gap: 0;
  margin-top: 56px;
  padding: 24px 0;
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
}
.stat { flex: 1; padding: 0 4px; }
.stat-num {
  font-family: var(--font-display);
  font-size: 30px;
  font-weight: 500;
  color: var(--primary);
  line-height: 1;
  font-variation-settings: "opsz" 96, "SOFT" 50;
}
.stat-num em { font-style: italic; font-weight: 400; color: var(--accent); margin-left: 2px; font-size: 22px; }
.stat-label {
  font-family: var(--font-body);
  font-size: 11px;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  color: var(--ink-mute);
  margin-top: 8px;
}
.stat-divider { width: 1px; background: var(--border); margin: 4px 0; }

/* 右侧:倾斜信件式聊天预览 */
.hero-visual {
  position: relative;
  perspective: 1200px;
}
.mock-chat {
  background: var(--paper-light);
  border-radius: var(--radius-lg);
  box-shadow:
    0 32px 64px rgba(74, 14, 26, 0.18),
    0 2px 0 var(--border-soft);
  border: 1px solid var(--border);
  overflow: hidden;
  transform: rotate(2deg);
  position: relative;
  animation: floatY 7s ease-in-out infinite;
}
/* 信件式做旧折角 */
.mock-chat::before {
  content: '';
  position: absolute;
  top: 0; right: 0;
  width: 38px; height: 38px;
  background: linear-gradient(225deg, var(--paper-deep) 50%, transparent 50%);
  box-shadow: -2px 2px 6px rgba(74, 14, 26, 0.08);
}
.mock-head {
  display: flex; align-items: center; gap: 12px;
  padding: 16px 20px;
  background: var(--primary);
  color: var(--paper-light);
  border-bottom: 2px solid var(--primary-dark);
}
.mock-avatar {
  width: 34px; height: 34px;
  border-radius: 50%;
  background: rgba(244, 235, 217, 0.18);
  display: inline-flex; align-items: center; justify-content: center;
  color: var(--paper-light);
}
.mock-name { font-family: var(--font-display); font-weight: 600; font-size: 16px; letter-spacing: 0.2px; }
.mock-status { margin-left: 8px; font-size: 12px; opacity: 0.8; font-weight: 400; font-family: var(--font-body); }
.mock-body {
  padding: 22px 20px;
  background:
    repeating-linear-gradient(0deg, transparent 0, transparent 32px, rgba(122, 31, 43, 0.04) 32px, rgba(122, 31, 43, 0.04) 33px),
    var(--paper-light);
  display: flex; flex-direction: column; gap: 14px;
}
.mock-row { display: flex; }
.mock-row.user { justify-content: flex-end; }
.mock-bubble {
  max-width: 82%;
  padding: 11px 16px;
  border-radius: 16px;
  font-size: 14.5px;
  line-height: 1.65;
  font-family: var(--font-body);
}
.mock-row.ai .mock-bubble {
  background: transparent;
  color: var(--ink);
  border: 1px solid var(--border);
  border-bottom-left-radius: 4px;
}
.mock-row.user .mock-bubble {
  background: var(--primary);
  color: var(--paper-light);
  border-bottom-right-radius: 4px;
}
/* 邮戳:右上角做旧圆形戳 */
.mock-stamp {
  position: absolute;
  top: -18px;
  right: -14px;
  z-index: 2;
  width: 78px; height: 78px;
  border-radius: 50%;
  border: 2px dashed var(--primary);
  color: var(--primary);
  background: rgba(244, 235, 217, 0.92);
  display: flex; align-items: center; justify-content: center;
  font-family: var(--font-display);
  font-style: italic;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 1.5px;
  text-align: center;
  line-height: 1.2;
  transform: rotate(-12deg);
  opacity: 0.78;
  animation: drift 10s ease-in-out infinite;
}

/* ===== 聊天区 ===== */
.chat-section {
  padding: 120px 0;
  background:
    radial-gradient(ellipse at 50% 0%, rgba(200, 144, 44, 0.06) 0, transparent 60%),
    var(--paper);
  position: relative;
}
.chat-section::before {
  content: '';
  position: absolute;
  top: 0; left: 50%;
  transform: translateX(-50%);
  width: 60%;
  max-width: 720px;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--border) 30%, var(--border) 70%, transparent);
}
.chat-wrap { max-width: 920px; margin: 0 auto; }
.chat-section .section-head { text-align: center; max-width: 680px; }
.chat-section .section-eyebrow { color: var(--accent-warm); }
.chat-section .section-eyebrow::before { background: var(--accent-warm); }

/* ===== CTA:牛血红实色 + 纸纹 ===== */
.cta-section {
  padding: 80px 0 120px;
  background: var(--paper);
}
.cta-card {
  position: relative;
  overflow: hidden;
  text-align: center;
  padding: 88px 32px;
  border-radius: var(--radius-lg);
  background: var(--primary);
  color: var(--paper-light);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--primary-dark);
}
/* 纸纹叠加 */
.cta-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='180' height='180'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='2' seed='4'/%3E%3CfeColorMatrix values='0 0 0 0 0.96 0 0 0 0 0.92 0 0 0 0 0.85 0 0 0 0.08 0'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
  opacity: 0.55;
  pointer-events: none;
  mix-blend-mode: overlay;
}
/* 大号衬线"Dear" 装饰 */
.cta-card::after {
  content: 'P.S.';
  position: absolute;
  top: -22px;
  left: 28px;
  font-family: var(--font-display);
  font-style: italic;
  font-size: 160px;
  color: var(--paper-light);
  opacity: 0.07;
  pointer-events: none;
  line-height: 1;
  font-weight: 300;
}
.cta-eyebrow {
  position: relative;
  z-index: 1;
  font-family: var(--font-display);
  font-style: italic;
  color: var(--accent-soft);
  font-size: 14px;
  letter-spacing: 2px;
  margin-bottom: 18px;
}
.cta-title {
  position: relative;
  z-index: 1;
  font-family: var(--font-display);
  font-size: clamp(28px, 4.4vw, 44px);
  margin: 0 0 18px;
  font-weight: 500;
  letter-spacing: -0.8px;
  line-height: 1.15;
  font-variation-settings: "opsz" 144, "SOFT" 80;
}
.cta-title em { font-style: italic; font-weight: 400; color: var(--accent-soft); }
.cta-sub {
  position: relative;
  z-index: 1;
  font-family: var(--font-body);
  font-size: 16px;
  opacity: 0.85;
  margin: 0 auto 36px;
  line-height: 1.75;
  max-width: 480px;
}
.cta-btn {
  position: relative;
  z-index: 1;
  background: var(--paper-light);
  color: var(--primary);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.22);
}
.cta-btn:hover { background: var(--paper); box-shadow: 0 16px 32px rgba(0, 0, 0, 0.28); }

/* ===== 页脚:深咖啡底 ===== */
.footer {
  margin-top: auto;
  background: #1f1410;
  color: #c9b699;
  padding: 72px 0 32px;
  position: relative;
}
.footer::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--primary) 0%, var(--accent-warm) 50%, var(--accent) 100%);
}
.footer-inner {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 56px;
  flex-wrap: wrap;
  padding-bottom: 40px;
  border-bottom: 1px solid rgba(244, 235, 217, 0.1);
}
.footer-brand .logo { color: var(--paper-light); align-items: center; }
.footer-logo-text { color: var(--paper-light); }
.footer-logo-text em { color: var(--accent-soft); }
.footer-desc {
  color: #8a7560;
  font-family: var(--font-body);
  font-size: 14px;
  line-height: 1.8;
  margin: 18px 0 0;
  max-width: 360px;
}
.footer-cols { display: flex; gap: 64px; flex-wrap: wrap; align-content: flex-start; }
.footer-col h4 {
  color: var(--paper-light);
  font-family: var(--font-display);
  font-style: italic;
  font-weight: 500;
  font-size: 16px;
  margin: 0 0 18px;
}
.footer-col a {
  display: block;
  color: #8a7560;
  font-family: var(--font-body);
  font-size: 14px;
  padding: 6px 0;
  transition: color 0.18s;
}
.footer-col a:hover { color: var(--accent-soft); }
.footer-bottom {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 28px;
  font-family: var(--font-body);
  font-size: 12px;
  color: #6b5a48;
  letter-spacing: 0.5px;
}

/* ===== 响应式 ===== */
@media (max-width: 1024px) {
  .paper-mark { display: none; }
}
@media (max-width: 960px) {
  .hero { padding: 72px 0 88px; }
  .hero-inner { grid-template-columns: 1fr; gap: 56px; }
  .hero-visual { max-width: 460px; margin: 0 auto; }
  .hero-copy { max-width: none; }
  .hero::before { font-size: clamp(140px, 36vw, 220px); }
}
@media (max-width: 860px) {
  .nav, .btn-register { display: none; }
  .menu-toggle { display: flex; }
  .nav.open {
    display: flex;
    flex-direction: column;
    position: absolute;
    top: 100%; left: 0; right: 0;
    background: var(--paper-light);
    backdrop-filter: blur(14px);
    padding: 18px 28px;
    gap: 6px;
    border-bottom: 1px solid var(--border);
  }
  .nav.open a { padding: 12px 4px; font-size: 15px; }
  .hero-stats { flex-wrap: wrap; gap: 16px 0; padding: 18px 0; }
  .stat { flex: 0 0 48%; }
  .stat-divider { display: none; }
  .footer-inner { grid-template-columns: 1fr; gap: 36px; }
  .footer-bottom { flex-direction: column; }
  .mock-stamp { width: 64px; height: 64px; font-size: 10px; top: -14px; right: -8px; }
}
@media (max-width: 560px) {
  .hero-title { font-size: 44px; }
  .stat { flex: 0 0 100%; }
  .hero-stats { gap: 18px; }
  .cta-card { padding: 64px 24px; }
}
</style>
