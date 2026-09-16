<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { API_BASE_URL } from '../api'

const messages = ref([])
const input = ref('')
const chatId = ref('')
const loading = ref(false)
let eventSource = null

// 快捷话题:点击即发送,降低新用户使用门槛
const quickPrompts = [
  { icon: '✦', text: '今天好累,陪我聊聊' },
  { icon: '✎', text: '讲个笑话给我听' },
  { icon: '♪', text: '推荐一首适合深夜的歌' },
  { icon: '☾', text: '睡不着,说点温柔的' },
]

onMounted(() => {
  chatId.value = 'love-' + Date.now()
  messages.value.push({
    role: 'ai',
    content: '你好呀,我是你的 AI 智能体。\n无论是日常琐事、感情烦恼,还是想找人说说话,我都在这里。试试下面的话题,或直接输入你想说的任何话。',
  })
})

function scrollToBottom() {
  nextTick(() => {
    const box = document.querySelector('.chat-box')
    if (box) box.scrollTop = box.scrollHeight
  })
}

function send(presetText) {
  const text = (presetText ?? input.value).trim()
  if (!text || loading.value) return
  messages.value.push({ role: 'user', content: text })
  messages.value.push({ role: 'ai', content: '' })
  input.value = ''
  loading.value = true
  scrollToBottom()

  const url = `${API_BASE_URL}/ai/love_app/chat/sse?message=${encodeURIComponent(text)}&chatId=${encodeURIComponent(chatId.value)}`
  eventSource = new EventSource(url)
  eventSource.onmessage = (e) => {
    messages.value[messages.value.length - 1].content += e.data
    scrollToBottom()
  }
  eventSource.onerror = () => {
    eventSource.close()
    // 若 AI 回复仍为空,给出友好提示,避免空白气泡
    const last = messages.value[messages.value.length - 1]
    if (last && last.role === 'ai' && !last.content) {
      last.content = '(连接已断开,请稍后重试)'
    }
    loading.value = false
  }
}

function stop() {
  if (eventSource) eventSource.close()
  loading.value = false
}

// 当前 AI 气泡是否处于"等待/打字中"状态
const isWaiting = computed(
  () => loading.value &&
  messages.value.length > 0 &&
  messages.value[messages.value.length - 1].role === 'ai' &&
  !messages.value[messages.value.length - 1].content
)
</script>

<template>
  <div class="chat">
    <div class="chat-head">
      <div class="avatar">
        <svg width="20" height="20" viewBox="0 0 100 100"><path d="M50 88 C20 65 10 45 10 30 C10 18 20 10 32 10 C40 10 46 14 50 22 C54 14 60 10 68 10 C80 10 90 18 90 30 C90 45 80 65 50 88 Z" fill="currentColor"/></svg>
      </div>
      <div class="chat-head-info">
        <div class="name">AI 智能体<em>·情书集</em></div>
        <div class="status">
          <span class="status-dot"></span>
          在线 · 流式响应 · 越聊越懂你
        </div>
      </div>
    </div>

    <div class="chat-box">
      <div
        v-for="(m, i) in messages"
        :key="i"
        :class="['row', m.role]"
      >
        <div class="avatar-sm" v-if="m.role === 'ai'">
          <svg width="14" height="14" viewBox="0 0 100 100"><path d="M50 88 C20 65 10 45 10 30 C10 18 20 10 32 10 C40 10 46 14 50 22 C54 14 60 10 68 10 C80 10 90 18 90 30 C90 45 80 65 50 88 Z" fill="currentColor"/></svg>
        </div>
        <div class="avatar-sm user" v-else>我</div>
        <div class="bubble" :class="{ typing: isWaiting && i === messages.length - 1 }">
          <template v-if="isWaiting && i === messages.length - 1">
            <span class="dot-anim"></span>
            <span class="dot-anim"></span>
            <span class="dot-anim"></span>
          </template>
          <template v-else>{{ m.content }}</template>
        </div>
      </div>
    </div>

    <!-- 快捷话题 -->
    <div class="quick-prompts">
      <button
        v-for="(p, i) in quickPrompts"
        :key="i"
        class="quick-chip"
        :disabled="loading"
        @click="send(p.text)"
      >
        <span class="chip-icon">{{ p.icon }}</span>{{ p.text }}
      </button>
    </div>

    <div class="input-row">
      <input
        v-model="input"
        @keyup.enter="send()"
        placeholder="和 AI 智能体说点什么..."
        :disabled="loading"
      />
      <button v-if="!loading" class="send" @click="send()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/></svg>
        发送
      </button>
      <button v-else class="stop" @click="stop">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><rect x="6" y="6" width="12" height="12" rx="2"/></svg>
        停止
      </button>
    </div>
  </div>
</template>

<style scoped>
.chat {
  display: flex;
  flex-direction: column;
  height: 70vh;
  max-height: 720px;
  min-height: 460px;
  background: var(--paper-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border);
}

/* 头部:牛血红实色 + 衬线名字 */
.chat-head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 22px;
  background: var(--primary);
  color: var(--paper-light);
  border-bottom: 2px solid var(--primary-dark);
  position: relative;
}
.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(244, 235, 217, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--paper-light);
  flex-shrink: 0;
}
.chat-head-info { position: relative; z-index: 1; }
.name {
  font-family: var(--font-display);
  font-weight: 600;
  font-size: 17px;
  letter-spacing: 0.2px;
}
.name em {
  font-style: italic;
  font-weight: 400;
  color: var(--accent-soft);
  margin-left: 6px;
  font-size: 13px;
}
.status {
  font-family: var(--font-body);
  font-size: 12px;
  opacity: 0.88;
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 3px;
}
.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #6dcf6d;
  box-shadow: 0 0 0 3px rgba(109, 207, 109, 0.3);
}

/* 聊天主体:信纸式横线背景 */
.chat-box {
  flex: 1;
  overflow-y: auto;
  padding: 24px 22px;
  background:
    repeating-linear-gradient(0deg, transparent 0, transparent 30px, rgba(122, 31, 43, 0.04) 30px, rgba(122, 31, 43, 0.04) 31px),
    var(--paper-light);
}
.chat-box::-webkit-scrollbar { width: 8px; }
.chat-box::-webkit-scrollbar-thumb { background: var(--border); border-radius: 99px; }
.chat-box::-webkit-scrollbar-track { background: transparent; }

.row {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;
  animation: msgIn 0.4s cubic-bezier(0.2, 0.7, 0.2, 1) both;
}
.row.user { flex-direction: row-reverse; }

.avatar-sm {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;
  background: var(--paper-light);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
  color: var(--primary);
  font-family: var(--font-display);
  font-style: italic;
  font-weight: 500;
}
.avatar-sm.user {
  background: var(--primary);
  color: var(--paper-light);
  border-color: var(--primary-dark);
}

.bubble {
  max-width: 76%;
  padding: 12px 16px;
  border-radius: 18px;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
  font-size: 14.5px;
  font-family: var(--font-body);
}
.row.user .bubble {
  background: var(--primary);
  color: var(--paper-light);
  border-bottom-right-radius: 4px;
  box-shadow: 0 4px 14px rgba(122, 31, 43, 0.18);
}
.row.ai .bubble {
  background: var(--card);
  color: var(--ink);
  border: 1px solid var(--border);
  border-bottom-left-radius: 4px;
  box-shadow: var(--shadow-sm);
}

/* 打字三点动画 */
.bubble.typing { display: flex; gap: 5px; align-items: center; padding: 16px 18px; }
.dot-anim { width: 7px; height: 7px; border-radius: 50%; background: var(--primary); animation: blink 1.3s infinite; }
.dot-anim:nth-child(2) { animation-delay: 0.2s; }
.dot-anim:nth-child(3) { animation-delay: 0.4s; }

/* 快捷话题 */
.quick-prompts {
  display: flex;
  gap: 8px;
  padding: 14px 18px 6px;
  overflow-x: auto;
  background: var(--paper-light);
  border-top: 1px solid var(--border-soft);
}
.quick-prompts::-webkit-scrollbar { display: none; }
.quick-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  padding: 8px 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-pill);
  background: var(--card);
  color: var(--ink-soft);
  font-family: var(--font-body);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}
.chip-icon {
  font-family: var(--font-display);
  font-style: italic;
  color: var(--accent);
  font-size: 14px;
}
.quick-chip:hover:not(:disabled) {
  background: var(--primary);
  border-color: var(--primary);
  color: var(--paper-light);
  transform: translateY(-1px);
}
.quick-chip:hover:not(:disabled) .chip-icon { color: var(--accent-soft); }
.quick-chip:disabled { opacity: 0.5; cursor: not-allowed; }

/* 输入区 */
.input-row {
  display: flex;
  gap: 8px;
  padding: 14px 18px 18px;
  border-top: 1px solid var(--border-soft);
  background: var(--paper-light);
}
.input-row input {
  flex: 1;
  padding: 12px 18px;
  border: 1px solid var(--border);
  border-radius: var(--radius-pill);
  outline: none;
  font-family: var(--font-body);
  font-size: 14.5px;
  background: var(--card);
  color: var(--ink);
  transition: border-color 0.2s, box-shadow 0.2s;
}
.input-row input::placeholder { color: var(--ink-faint); }
.input-row input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 4px rgba(122, 31, 43, 0.1);
}
.input-row input:disabled { opacity: 0.6; }

.send, .stop {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 22px;
  border: none;
  border-radius: var(--radius-pill);
  cursor: pointer;
  font-family: var(--font-body);
  font-weight: 700;
  font-size: 14px;
  letter-spacing: 0.3px;
  transition: transform 0.2s, box-shadow 0.2s, background 0.2s;
}
.send {
  background: var(--primary);
  color: var(--paper-light);
  box-shadow: 0 4px 14px rgba(122, 31, 43, 0.24);
}
.send:hover { transform: translateY(-1px); background: var(--primary-dark); box-shadow: 0 8px 20px rgba(122, 31, 43, 0.34); }
.stop {
  background: var(--accent);
  color: var(--paper-light);
}
.stop:hover { background: #b07d1f; }

@media (max-width: 640px) {
  .chat { height: 64vh; min-height: 420px; border-radius: var(--radius); }
  .bubble { max-width: 82%; font-size: 14px; }
  .avatar-sm { width: 28px; height: 28px; font-size: 11px; }
  .name { font-size: 15px; }
  .name em { font-size: 12px; }
}
</style>
