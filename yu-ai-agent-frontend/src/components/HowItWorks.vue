<script setup>
const steps = [
  { num: '01', title: '说出你的话', desc: '在对话框里输入任何想说的话,或点击推荐话题快速开始。无需注册,直接上手。', icon: '✎' },
  { num: '02', title: 'AI 实时回应', desc: '流式输出,AI 边思考边回答。你能看到文字一个个出现,就像真人在打字。', icon: '✦' },
  { num: '03', title: '越聊越懂你', desc: 'AI 会记住对话上下文,深入交流。聊得越多,回应越贴合你的状态与情绪。', icon: '❤' },
]
</script>

<template>
  <section class="section steps" id="steps">
    <div class="container">
      <div class="section-head reveal">
        <span class="section-eyebrow">· 使用流程 ·</span>
        <h2 class="section-title">三步开始 <span class="gradient-text">你的 AI 陪伴</span></h2>
        <p class="section-subtitle">简单三步,从打开页面到获得温暖回应,只需几秒。</p>
      </div>

      <div class="steps-grid">
        <div
          v-for="(s, i) in steps"
          :key="i"
          :class="['step', 'reveal', `reveal-delay-${i + 1}`]"
        >
          <div class="step-num">{{ s.num }}</div>
          <div class="step-icon">{{ s.icon }}</div>
          <h3 class="step-title">{{ s.title }}</h3>
          <p class="step-desc">{{ s.desc }}</p>
          <div v-if="i < steps.length - 1" class="step-arrow">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14M13 6l6 6-6 6"/></svg>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.steps {
  background:
    linear-gradient(180deg, var(--paper-light) 0%, var(--paper) 100%);
  position: relative;
}
.steps::before {
  content: '';
  position: absolute;
  top: 0; left: 50%;
  transform: translateX(-50%);
  width: 60%; max-width: 720px;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--border) 30%, var(--border) 70%, transparent);
}

.steps-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
  position: relative;
}

.step {
  position: relative;
  padding: 40px 30px 36px;
  border-radius: var(--radius-lg);
  background: var(--card);
  border: 1px solid var(--border);
  border-top: 3px solid var(--primary);
  transition: transform 0.32s cubic-bezier(0.2, 0.7, 0.2, 1), box-shadow 0.32s ease;
}
.step:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

/* 大斜体编号:杂志栏目感 */
.step-num {
  font-family: var(--font-display);
  font-style: italic;
  font-size: 64px;
  font-weight: 300;
  color: var(--primary);
  line-height: 0.95;
  margin-bottom: 20px;
  font-variation-settings: "opsz" 144, "SOFT" 80;
  letter-spacing: -2px;
}

.step-icon {
  width: 48px;
  height: 48px;
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

.step-title {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 12px;
  color: var(--ink);
  letter-spacing: -0.3px;
}

.step-desc {
  font-family: var(--font-body);
  font-size: 14.5px;
  line-height: 1.75;
  color: var(--ink-soft);
  margin: 0;
}

/* 步骤间墨水箭头 */
.step-arrow {
  position: absolute;
  top: 50%;
  right: -26px;
  transform: translateY(-50%);
  z-index: 2;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--paper);
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary);
  box-shadow: var(--shadow-sm);
}

@media (max-width: 900px) {
  .steps-grid { grid-template-columns: 1fr; gap: 24px; }
  .step-arrow { display: none; }
  .step-num { font-size: 48px; }
}
</style>
