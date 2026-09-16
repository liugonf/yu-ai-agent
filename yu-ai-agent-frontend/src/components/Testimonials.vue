<script setup>
const testimonials = [
  { name: '小柔', tag: '深夜党', avatar: '柔', text: '失眠的夜里,它真的像朋友一样在听。回复很温柔,不像机器,陪我度过了很多难熬的夜晚。', rating: 5 },
  { name: '阿哲', tag: '打工人', avatar: '哲', text: '上班压力大,随手聊两句就能缓过来。流式输出很丝滑,越聊越懂我的状态,有点上瘾。', rating: 5 },
  { name: '林林', tag: '学生党', avatar: '林', text: '考前焦虑找它倾诉,比想象中贴心。还会讲笑话缓解情绪,推荐话题很好用,直接点就行。', rating: 5 },
]
</script>

<template>
  <section class="section testimonials">
    <div class="container">
      <div class="section-head reveal">
        <span class="section-eyebrow">· 用户口碑 ·</span>
        <h2 class="section-title">他们都说 <span class="gradient-text">越聊越离不开</span></h2>
        <p class="section-subtitle">真实用户的陪伴感受,这是 AI 能给到的温度。</p>
      </div>

      <div class="t-grid">
        <div
          v-for="(t, i) in testimonials"
          :key="i"
          :class="['t-card', 'reveal', `reveal-delay-${i + 1}`]"
        >
          <div class="t-quote">"</div>
          <div class="t-stars">
            <span v-for="n in t.rating" :key="n">★</span>
          </div>
          <p class="t-text">{{ t.text }}</p>
          <div class="t-user">
            <div class="t-avatar">{{ t.avatar }}</div>
            <div>
              <div class="t-name">{{ t.name }}</div>
              <div class="t-tag">{{ t.tag }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.testimonials {
  background:
    linear-gradient(180deg, var(--paper) 0%, var(--paper-light) 100%);
  position: relative;
}
.testimonials::before {
  content: '';
  position: absolute;
  top: 0; left: 50%;
  transform: translateX(-50%);
  width: 60%; max-width: 720px;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--border) 30%, var(--border) 70%, transparent);
}

.t-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 26px;
}

/* 便签式卡片:暖纸 + 折角 + 信纸横线 */
.t-card {
  position: relative;
  padding: 32px 28px 28px;
  border-radius: var(--radius-lg);
  background: var(--card);
  border: 1px solid var(--border);
  transition: transform 0.32s cubic-bezier(0.2, 0.7, 0.2, 1), box-shadow 0.32s ease;
  overflow: hidden;
}
.t-card::before {
  content: '';
  position: absolute;
  top: 0; right: 0;
  width: 28px; height: 28px;
  background: linear-gradient(225deg, var(--paper-deep) 50%, transparent 50%);
}
.t-card:hover {
  transform: translateY(-4px) rotate(-0.3deg);
  box-shadow: var(--shadow-md);
}

/* 巨大斜体引号装饰 */
.t-quote {
  position: absolute;
  top: -8px;
  left: 18px;
  font-family: var(--font-display);
  font-style: italic;
  font-weight: 400;
  font-size: 96px;
  color: var(--primary);
  opacity: 0.12;
  line-height: 1;
  pointer-events: none;
  font-variation-settings: "opsz" 144, "SOFT" 80;
}

.t-stars {
  color: var(--accent);
  font-size: 14px;
  letter-spacing: 3px;
  margin-bottom: 14px;
  position: relative;
  z-index: 1;
}

.t-text {
  font-family: var(--font-display);
  font-style: italic;
  font-size: 16px;
  line-height: 1.75;
  color: var(--ink);
  margin: 0 0 24px;
  position: relative;
  z-index: 1;
  font-variation-settings: "opsz" 96, "SOFT" 50;
}

.t-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 18px;
  border-top: 1px solid var(--border-soft);
}
.t-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--primary);
  color: var(--paper-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-style: italic;
  font-weight: 500;
  font-size: 16px;
}
.t-name {
  font-family: var(--font-body);
  font-weight: 700;
  font-size: 14px;
  color: var(--ink);
}
.t-tag {
  font-family: var(--font-display);
  font-style: italic;
  font-size: 12px;
  color: var(--ink-mute);
  margin-top: 2px;
}

@media (max-width: 900px) {
  .t-grid { grid-template-columns: 1fr; }
  .t-card:hover { transform: translateY(-4px); }
}
@media (max-width: 560px) {
  .t-card { padding: 26px 22px 22px; }
  .t-text { font-size: 15px; }
  .t-quote { font-size: 72px; top: -4px; left: 12px; }
  .t-card::before { width: 22px; height: 22px; }
}
</style>
