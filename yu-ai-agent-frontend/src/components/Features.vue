<script setup>
// 第 0 项作为旗舰能力,占据更大版面、采用差异化视觉,避免"6 张相同卡片"反模式
const features = [
  { num: '01', icon: '✎', title: '流式对话', desc: '实时流式响应,边思考边输出,文字像真人一样一个个出现。丝滑、有温度,告别等待。', featured: true, tag: '旗舰能力' },
  { num: '02', icon: ' ◐', title: '语音交互', desc: '支持语音输入与对话,解放双手。' },
  { num: '03', icon: ' ◑', title: '图像识别', desc: '发送图片 AI 也能看懂,分享生活点滴。' },
  { num: '04', icon: ' ❤', title: '情感理解', desc: '懂你情绪起伏,用恰到好处的温度回应。' },
  { num: '05', icon: ' ✦', title: '隐私安全', desc: '对话加密处理,你的故事只属于你。' },
  { num: '06', icon: ' ⚡', title: '极速响应', desc: '毫秒级回复,24 小时全天在线。' },
]
</script>

<template>
  <section class="section features" id="features">
    <div class="container">
      <div class="section-head reveal">
        <span class="section-eyebrow">· 核心能力 ·</span>
        <h2 class="section-title">为什么选择 <span class="gradient-text">AI 超级智能体</span></h2>
        <p class="section-subtitle">不止能聊,更懂你。六大能力支撑真正有温度的陪伴。</p>
      </div>

      <div class="grid">
        <article
          v-for="(f, i) in features"
          :key="i"
          :class="['card', { featured: f.featured }, 'reveal', `reveal-delay-${Math.min(i, 6) + 1}`]"
        >
          <span v-if="f.featured" class="card-tag">{{ f.tag }}</span>
          <div class="card-num">{{ f.num }}</div>
          <div class="card-icon">{{ f.icon }}</div>
          <h3 class="card-title">{{ f.title }}</h3>
          <p class="card-desc">{{ f.desc }}</p>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.features {
  background:
    linear-gradient(180deg, var(--paper) 0%, var(--paper-light) 100%);
  position: relative;
}
/* 上沿分隔线 */
.features::before {
  content: '';
  position: absolute;
  top: 0; left: 50%;
  transform: translateX(-50%);
  width: 60%; max-width: 720px;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--border) 30%, var(--border) 70%, transparent);
}

/* bento 布局:3×3,首卡跨 2×2 做旗舰 */
.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-auto-rows: minmax(190px, auto);
  gap: 22px;
}

.card {
  position: relative;
  overflow: hidden;
  padding: 28px 26px;
  border-radius: var(--radius-lg);
  background: var(--card);
  border: 1px solid var(--border);
  transition: transform 0.32s cubic-bezier(0.2, 0.7, 0.2, 1), box-shadow 0.32s ease, border-color 0.32s ease;
  display: flex;
  flex-direction: column;
}
.card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary);
}

/* 大号编号:像杂志栏目序号 */
.card-num {
  position: absolute;
  top: 20px;
  right: 24px;
  font-family: var(--font-display);
  font-style: italic;
  font-size: 44px;
  font-weight: 300;
  color: var(--ink-faint);
  line-height: 1;
  font-variation-settings: "opsz" 144, "SOFT" 80;
  opacity: 0.5;
  transition: color 0.32s;
}
.card:hover .card-num { color: var(--primary); opacity: 0.85; }

.card-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: var(--paper-dark);
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: var(--primary);
  margin-bottom: 18px;
}
.card-title {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 8px;
  color: var(--ink);
  letter-spacing: -0.3px;
}
.card-desc {
  font-family: var(--font-body);
  font-size: 14px;
  line-height: 1.7;
  color: var(--ink-soft);
  margin: 0;
}

/* 旗舰卡:跨 2 列 2 行,深牛血红实色,大号斜体衬线标题 */
.card.featured {
  grid-column: span 2;
  grid-row: span 2;
  background: var(--primary);
  border: 1px solid var(--primary-dark);
  color: var(--paper-light);
  justify-content: flex-end;
  padding: 40px 38px;
  box-shadow: var(--shadow-lg);
  position: relative;
  overflow: hidden;
}
/* 纙纹叠加 */
.card.featured::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='180' height='180'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='2' seed='4'/%3E%3CfeColorMatrix values='0 0 0 0 0.96 0 0 0 0 0.92 0 0 0 0 0.85 0 0 0 0.07 0'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
  opacity: 0.55;
  pointer-events: none;
  mix-blend-mode: overlay;
}
/* 巨大斜体 "01" 装饰 */
.card.featured::after {
  content: '01';
  position: absolute;
  top: -30px;
  right: -10px;
  font-family: var(--font-display);
  font-style: italic;
  font-weight: 300;
  font-size: 220px;
  color: var(--paper-light);
  opacity: 0.08;
  line-height: 1;
  pointer-events: none;
}
.card.featured .card-num { display: none; }
.card.featured .card-icon {
  background: rgba(244, 235, 217, 0.14);
  border: 1px solid rgba(244, 235, 217, 0.2);
  color: var(--accent-soft);
  width: 58px; height: 58px;
  font-size: 28px;
  margin-bottom: 22px;
  position: relative;
  z-index: 1;
}
.card.featured .card-title {
  font-family: var(--font-display);
  font-size: clamp(28px, 3.5vw, 36px);
  font-weight: 500;
  color: var(--paper-light);
  margin-bottom: 14px;
  letter-spacing: -0.6px;
  line-height: 1.15;
  font-variation-settings: "opsz" 144, "SOFT" 80;
  position: relative;
  z-index: 1;
}
.card.featured .card-title em { font-style: italic; color: var(--accent-soft); }
.card.featured .card-desc {
  font-size: 15.5px;
  line-height: 1.78;
  color: rgba(244, 235, 217, 0.88);
  max-width: 460px;
  position: relative;
  z-index: 1;
}
.card-tag {
  position: absolute;
  top: 26px;
  left: 26px;
  z-index: 2;
  padding: 6px 14px;
  border-radius: var(--radius-pill);
  background: rgba(244, 235, 217, 0.16);
  backdrop-filter: blur(6px);
  color: var(--paper-light);
  font-family: var(--font-body);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1.5px;
  text-transform: uppercase;
}

@media (max-width: 900px) {
  .grid { grid-template-columns: repeat(2, 1fr); }
  .card.featured { grid-column: span 2; grid-row: span 1; }
  .card.featured::after { font-size: 140px; top: -16px; }
}
@media (max-width: 560px) {
  .grid { grid-template-columns: 1fr; }
  .card.featured { grid-column: span 1; }
  .card.featured::after { font-size: 110px; }
}
</style>
