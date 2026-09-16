<script setup>
import { ref } from 'vue'

const faqs = [
  { q: '使用需要注册或付费吗?', open: true, a: '完全不需要。打开页面即可直接开始对话,免费体验全部核心能力,没有门槛。' },
  { q: '我的对话会被保存吗?隐私安全吗?', open: false, a: '对话内容仅用于维持当前会话上下文,我们重视你的隐私。可以放心倾诉,不会被公开。' },
  { q: '支持语音和图片吗?', open: false, a: '支持。除了文字聊天,智能体还支持语音交互与图像识别,你可以发送图片让它"看懂"你的世界。' },
  { q: '为什么回复是一个字一个字出现的?', open: false, a: '这是流式响应技术——AI 边思考边输出,让你无需等待整段回复,体验更接近真人对话,也更高效。' },
  { q: '它能记住我之前说过的话吗?', open: false, a: '可以。在同一个会话内,AI 会记住上下文,聊得越多越懂你。开始新对话时会重置上下文。' },
]

const list = ref(faqs)
function toggle(i) {
  list.value[i].open = !list.value[i].open
}
</script>

<template>
  <section class="section faq" id="faq">
    <div class="container narrow">
      <div class="section-head reveal">
        <span class="section-eyebrow">· 常见问题 ·</span>
        <h2 class="section-title">你可能想问的</h2>
        <p class="section-subtitle">关于使用、隐私、能力的常见疑问,一图看懂。</p>
      </div>

      <div class="faq-list">
        <div
          v-for="(f, i) in list"
          :key="i"
          :class="['faq-item', 'reveal', { open: f.open }]"
        >
          <button class="faq-q" @click="toggle(i)">
            <span class="faq-q-mark">Q.</span>
            <span class="faq-q-text">{{ f.q }}</span>
            <span class="faq-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M6 9l6 6 6-6"/></svg>
            </span>
          </button>
          <div class="faq-a">
            <span class="faq-a-mark">A.</span>
            <p>{{ f.a }}</p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.faq {
  background:
    linear-gradient(180deg, var(--paper-light) 0%, var(--paper) 100%);
  position: relative;
}
.faq::before {
  content: '';
  position: absolute;
  top: 0; left: 50%;
  transform: translateX(-50%);
  width: 60%; max-width: 720px;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--border) 30%, var(--border) 70%, transparent);
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.faq-item {
  border-radius: var(--radius);
  background: var(--card);
  border: 1px solid var(--border);
  border-left: 3px solid var(--border);
  overflow: hidden;
  transition: box-shadow 0.22s, border-color 0.22s;
}
.faq-item.open {
  box-shadow: var(--shadow-sm);
  border-left-color: var(--primary);
}

.faq-q {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 22px;
  background: none;
  border: none;
  cursor: pointer;
  text-align: left;
  color: var(--ink);
}
.faq-q-mark {
  font-family: var(--font-display);
  font-style: italic;
  font-weight: 500;
  font-size: 22px;
  color: var(--accent);
  flex-shrink: 0;
  line-height: 1;
  font-variation-settings: "opsz" 96, "SOFT" 50;
}
.faq-q-text {
  flex: 1;
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 600;
  letter-spacing: -0.2px;
  color: var(--ink);
}
.faq-icon {
  flex-shrink: 0;
  color: var(--ink-mute);
  display: flex;
  transition: transform 0.32s cubic-bezier(0.2, 0.7, 0.2, 1), color 0.22s;
}
.faq-item.open .faq-icon { transform: rotate(180deg); color: var(--primary); }

.faq-a {
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.36s cubic-bezier(0.2, 0.7, 0.2, 1);
}
.faq-item.open .faq-a { max-height: 280px; }
.faq-a p {
  margin: 0;
  padding: 4px 22px 22px 56px;
  color: var(--ink-soft);
  font-family: var(--font-body);
  font-size: 14.5px;
  line-height: 1.8;
}
.faq-a-mark {
  display: none;
}
</style>
