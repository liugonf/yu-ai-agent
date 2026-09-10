<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { API_BASE_URL } from '../api'

const messages = ref([])
const input = ref('')
const chatId = ref('')
const loading = ref(false)
let eventSource = null

onMounted(() => {
  chatId.value = 'love-' + Date.now()
  // 开场欢迎语
  messages.value.push({
    role: 'ai',
    content: '你好呀～我是你的 AI 恋爱顾问。无论是单身、恋爱还是婚姻中的困惑，都可以跟我说说 💜',
  })
})

function scrollToBottom() {
  nextTick(() => {
    const box = document.querySelector('.chat-box')
    if (box) box.scrollTop = box.scrollHeight
  })
}

function send() {
  const text = input.value.trim()
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
    loading.value = false
  }
}

function stop() {
  if (eventSource) eventSource.close()
  loading.value = false
}
</script>

<template>
  <div class="chat">
    <div class="chat-head">
      <div class="avatar">💜</div>
      <div>
        <div class="name">AI 恋爱顾问</div>
        <div class="status">在线 · 用心倾听每一段感情</div>
      </div>
    </div>
    <div class="chat-box">
      <div v-for="(m, i) in messages" :key="i" :class="['row', m.role]">
        <div class="bubble">{{ m.content }}</div>
      </div>
      <div v-if="loading" class="row ai"><div class="bubble typing">正在输入…</div></div>
    </div>
    <div class="input-row">
      <input v-model="input" @keyup.enter="send" placeholder="和恋爱大师聊聊吧…" />
      <button class="send" @click="send">发送</button>
      <button v-if="loading" class="stop" @click="stop">停止</button>
    </div>
  </div>
</template>

<style scoped>
.chat { display: flex; flex-direction: column; height: 68vh; background: #fff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 30px rgba(124, 77, 255, 0.12); }
.chat-head { display: flex; align-items: center; gap: 12px; padding: 14px 18px; background: linear-gradient(135deg, #8b5cf6, #6d28d9); color: #fff; }
.avatar { width: 42px; height: 42px; border-radius: 50%; background: rgba(255,255,255,0.25); display: flex; align-items: center; justify-content: center; font-size: 20px; }
.name { font-weight: 700; }
.status { font-size: 12px; opacity: 0.9; }

.chat-box { flex: 1; overflow-y: auto; padding: 18px; background: #faf8ff; }
.row { display: flex; margin-bottom: 12px; }
.row.user { justify-content: flex-end; }
.row.ai { justify-content: flex-start; }
.bubble { max-width: 72%; padding: 12px 16px; border-radius: 16px; white-space: pre-wrap; word-break: break-word; line-height: 1.6; }
.row.user .bubble { background: linear-gradient(135deg, #8b5cf6, #6d28d9); color: #fff; border-bottom-right-radius: 4px; }
.row.ai .bubble { background: #ede9fe; color: #1f2937; border-bottom-left-radius: 4px; }
.typing { color: #8b5cf6; }

.input-row { display: flex; gap: 8px; padding: 12px; border-top: 1px solid #efe9ff; }
.input-row input { flex: 1; padding: 12px; border: 1px solid #e5dffb; border-radius: 999px; outline: none; }
.send, .stop { padding: 0 20px; border: none; border-radius: 999px; cursor: pointer; color: #fff; font-weight: 600; }
.send { background: linear-gradient(135deg, #8b5cf6, #6d28d9); }
.stop { background: #c4b5fd; }
</style>
