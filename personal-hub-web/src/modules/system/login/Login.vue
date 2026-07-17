<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/authStore'
import { ElMessage } from 'element-plus'
import {
  Layers, User, Lock, FileText, BookMarked, CheckSquare, BarChart3, ArrowRight,
  BookOpen, Code2, Database, Hash, Braces, Coffee,
} from 'lucide-vue-next'
import BookShelfCaptcha from './BookShelfCaptcha.vue'

const REMEMBER_KEY = 'ph-login-remember'

const authStore = useAuthStore()
const router = useRouter()
const form = ref({ username: 'admin', password: '123456' })
const rememberMe = ref(true)
const loading = ref(false)
const loginSuccess = ref(false)
const pageLeaving = ref(false)
const captchaReady = ref(false)
const captchaId = ref('')
const sliderX = ref(0)
const captchaRef = ref<InstanceType<typeof BookShelfCaptcha> | null>(null)
/** form → loading → verify */
const step = ref<'form' | 'loading' | 'verify'>('form')
let verifyTimer: ReturnType<typeof setTimeout> | null = null
let submitting = false
let clockTimer: ReturnType<typeof setInterval> | null = null

/** 工作台时间 / 问候 */
const now = ref(new Date())
const clockText = computed(() =>
  now.value.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit', hour12: false }),
)
const greeting = computed(() => {
  const h = now.value.getHours()
  if (h < 5) return 'Good Night'
  if (h < 12) return 'Good Morning'
  if (h < 18) return 'Good Afternoon'
  return 'Good Evening'
})

const DAILY_QUOTES = [
  'The expert in anything was once a beginner.',
  'Knowledge grows quietly when you show up every day.',
  'Read deeply. Write clearly. Build carefully.',
  'Small notes compound into lasting insight.',
  'Clarity is the ultimate sophistication.',
  'Stay curious. Stay kind. Keep shipping.',
  'Your future self is watching what you learn today.',
]

const dailyQuote = computed(() => {
  const start = new Date(now.value.getFullYear(), 0, 0)
  const day = Math.floor((now.value.getTime() - start.getTime()) / 86_400_000)
  return DAILY_QUOTES[((day % DAILY_QUOTES.length) + DAILY_QUOTES.length) % DAILY_QUOTES.length]
})

/** 知识元素水印（opacity 极低漂浮） */
const knowledgeMarks = [
  { icon: Hash, label: 'Markdown', top: '10%', left: '12%', dur: '34s', delay: '0s' },
  { icon: BookOpen, label: 'Book', top: '22%', left: '68%', dur: '38s', delay: '-6s' },
  { icon: Code2, label: 'Code', top: '48%', left: '18%', dur: '36s', delay: '-12s' },
  { icon: Database, label: 'SQL', top: '62%', left: '58%', dur: '40s', delay: '-4s' },
  { icon: Braces, label: 'Vue', top: '78%', left: '28%', dur: '32s', delay: '-18s' },
  { icon: Coffee, label: 'Spring', top: '36%', left: '82%', dur: '37s', delay: '-10s' },
]

/** 鼠标视差（-0.5 ~ 0.5） */
const pointer = ref({ x: 0, y: 0 })
let raf = 0
let target = { x: 0, y: 0 }

function onPointerMove(e: PointerEvent) {
  const w = window.innerWidth || 1
  const h = window.innerHeight || 1
  target = {
    x: (e.clientX / w - 0.5),
    y: (e.clientY / h - 0.5),
  }
  if (!raf) raf = requestAnimationFrame(tickPointer)
}

function tickPointer() {
  pointer.value = {
    x: pointer.value.x + (target.x - pointer.value.x) * 0.08,
    y: pointer.value.y + (target.y - pointer.value.y) * 0.08,
  }
  const dx = Math.abs(pointer.value.x - target.x)
  const dy = Math.abs(pointer.value.y - target.y)
  if (dx > 0.001 || dy > 0.001) {
    raf = requestAnimationFrame(tickPointer)
  } else {
    raf = 0
  }
}

const brandParallax = () => ({
  transform: `translate3d(${pointer.value.x * -10}px, ${pointer.value.y * -6}px, 0)`,
})
const cardParallax = () => ({
  transform: `translate3d(${pointer.value.x * 8}px, ${pointer.value.y * 6}px, 0)`,
})
const spotlightStyle = () => ({
  background: `radial-gradient(520px circle at ${50 + pointer.value.x * 40}% ${50 + pointer.value.y * 40}%, rgba(22,163,74,0.06), transparent 55%)`,
})

const features = [
  { icon: FileText, label: 'Notes' },
  { icon: BookMarked, label: 'Reading' },
  { icon: CheckSquare, label: 'Tasks' },
  { icon: BarChart3, label: 'Statistics' },
]

onMounted(() => {
  window.addEventListener('pointermove', onPointerMove, { passive: true })
  clockTimer = setInterval(() => { now.value = new Date() }, 30_000)
  try {
    const raw = localStorage.getItem(REMEMBER_KEY)
    if (raw) {
      const saved = JSON.parse(raw) as { username?: string; password?: string }
      if (saved.username) form.value.username = saved.username
      if (saved.password) form.value.password = saved.password
      rememberMe.value = true
    }
  } catch { /* ignore */ }
})

onUnmounted(() => {
  window.removeEventListener('pointermove', onPointerMove)
  if (raf) cancelAnimationFrame(raf)
  if (verifyTimer) clearTimeout(verifyTimer)
  if (clockTimer) clearInterval(clockTimer)
})

function onCaptchaChange(payload: { captchaId: string; sliderX: number; ready: boolean }) {
  captchaId.value = payload.captchaId
  sliderX.value = payload.sliderX
  captchaReady.value = payload.ready
  if (payload.ready && step.value === 'verify' && !submitting) {
    void finishLogin()
  }
}

/** 第一步：校验账号密码后进入 Loading → 安全验证 */
async function handleLogin() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  if (step.value !== 'form' || loading.value) return

  loading.value = true
  step.value = 'loading'
  captchaReady.value = false
  captchaId.value = ''
  sliderX.value = 0

  if (verifyTimer) clearTimeout(verifyTimer)
  verifyTimer = setTimeout(() => {
    loading.value = false
    step.value = 'verify'
  }, 720)
}

/** 第二步：书架验证通过后真正登录 */
async function finishLogin() {
  if (submitting || !captchaReady.value || !captchaId.value) return
  submitting = true
  loading.value = true
  try {
    if (rememberMe.value) {
      localStorage.setItem(REMEMBER_KEY, JSON.stringify({
        username: form.value.username,
        password: form.value.password,
      }))
    } else {
      localStorage.removeItem(REMEMBER_KEY)
    }
    await authStore.login(
      form.value.username,
      form.value.password,
      captchaId.value,
      sliderX.value,
      { redirect: false },
    )
    loginSuccess.value = true
    await nextTick()
    pageLeaving.value = true
    window.setTimeout(() => {
      router.push('/')
    }, 480)
  } catch (e: any) {
    const msg = e?.response?.data?.message || '登录失败'
    ElMessage.error(msg)
    captchaRef.value?.refresh()
    captchaReady.value = false
    step.value = 'verify'
  } finally {
    loading.value = false
    submitting = false
  }
}

function backToForm() {
  if (verifyTimer) clearTimeout(verifyTimer)
  step.value = 'form'
  loading.value = false
  captchaReady.value = false
  captchaId.value = ''
  sliderX.value = 0
}

function onForgotPassword() {
  ElMessage.info('忘记密码功能即将支持')
}
</script>

<template>
  <div class="login-page" :class="{ leaving: pageLeaving }">
    <div class="ambient" aria-hidden="true">
      <div class="ambient-grid" />
      <div class="ambient-glow ambient-glow--a" />
      <div class="ambient-glow ambient-glow--b" />
      <div class="knowledge-float">
        <div
          v-for="k in knowledgeMarks"
          :key="k.label"
          class="kf"
          :style="{
            top: k.top,
            left: k.left,
            animationDuration: k.dur,
            animationDelay: k.delay,
          }"
        >
          <component :is="k.icon" :size="22" stroke-width="1.5" />
          <span>{{ k.label }}</span>
        </div>
      </div>
      <div class="ambient-spotlight" :style="spotlightStyle()" />
    </div>

    <aside class="brand">
      <div class="brand-inner" :style="brandParallax()">
        <div class="workspace-meta anim anim-1">
          <span class="greeting">{{ greeting }}</span>
          <span class="clock-dot" />
          <time class="clock" :datetime="now.toISOString()">{{ clockText }}</time>
        </div>

        <div class="logo anim anim-2">
          <span class="logo-mark"><Layers :size="18" stroke-width="1.75" /></span>
          <div class="logo-copy">
            <span class="logo-name">Personal Hub</span>
            <span class="logo-os">Knowledge OS</span>
          </div>
        </div>

        <h1 class="brand-title anim anim-3">你的知识，安静生长</h1>
        <p class="brand-desc anim anim-4">
          统一管理笔记、阅读、学习、任务、计划与收藏，<br />
          打造属于自己的知识工作台。
        </p>

        <ul class="features anim anim-5">
          <li v-for="f in features" :key="f.label" class="feature">
            <component :is="f.icon" :size="15" stroke-width="1.75" />
            <span>{{ f.label }}</span>
          </li>
        </ul>
      </div>

      <blockquote class="quote anim anim-6">
        <span class="quote-label">每日一句</span>
        <p class="quote-text">“{{ dailyQuote }}”</p>
      </blockquote>
    </aside>

    <section class="panel">
      <div class="card anim-card" :style="cardParallax()" :class="[`step-${step}`]">
        <Transition name="step" mode="out-in">
          <!-- 步骤 1：账号密码 -->
          <div v-if="step === 'form'" key="form" class="step-pane">
            <header class="card-head">
              <h2 class="card-title">欢迎回来 👋</h2>
              <p class="card-sub">登录你的 Personal Hub</p>
            </header>

            <form class="form" @submit.prevent="handleLogin">
              <label class="field">
                <span class="field-label">用户名</span>
                <div class="field-shell">
                  <User class="field-icon" :size="16" stroke-width="1.75" />
                  <input
                    v-model="form.username"
                    class="field-input"
                    type="text"
                    autocomplete="username"
                    placeholder="输入用户名"
                  />
                </div>
              </label>

              <label class="field">
                <span class="field-label">密码</span>
                <div class="field-shell">
                  <Lock class="field-icon" :size="16" stroke-width="1.75" />
                  <input
                    v-model="form.password"
                    class="field-input"
                    type="password"
                    autocomplete="current-password"
                    placeholder="输入密码"
                    @keyup.enter="handleLogin"
                  />
                </div>
              </label>

              <button type="submit" class="submit" :disabled="loading">
                <span>进入 Personal Hub</span>
                <ArrowRight :size="16" stroke-width="2" />
              </button>

              <div class="meta">
                <label class="remember">
                  <input v-model="rememberMe" type="checkbox" />
                  <span>记住我</span>
                </label>
                <button type="button" class="ghost-link" @click="onForgotPassword">忘记密码</button>
              </div>
            </form>
          </div>

          <!-- 步骤 2：Loading -->
          <div v-else-if="step === 'loading'" key="loading" class="step-pane step-pane--center">
            <div class="loading-block">
              <div class="loading-spinner" />
              <p class="loading-text">正在验证身份…</p>
            </div>
          </div>

          <!-- 步骤 3：安全验证（书架） -->
          <div v-else key="verify" class="step-pane">
            <header class="card-head">
              <button type="button" class="back-btn" @click="backToForm">← 返回</button>
              <h2 class="card-title">安全验证</h2>
              <p class="card-sub">请完成知识验证</p>
            </header>

            <BookShelfCaptcha ref="captchaRef" class="captcha" @change="onCaptchaChange" />

            <p v-if="loading && !loginSuccess" class="verify-hint">登录中…</p>
            <p v-else-if="loginSuccess" class="verify-hint ok">✔ Success</p>
            <p v-else class="verify-hint">将书本拖入空位即可进入</p>
          </div>
        </Transition>
      </div>
    </section>
  </div>
</template>

<style scoped>
.login-page {
  --brand: #16a34a;
  --brand-deep: #15803d;
  --bg: #f7f7f5;
  --text: #111827;
  --muted: #6b7280;
  --line: #e5e7eb;
  --ease: cubic-bezier(0.22, 1, 0.36, 1);

  position: relative;
  display: grid;
  grid-template-columns: 55% 45%;
  min-height: 100vh;
  background: var(--bg);
  color: var(--text);
  font-family: "Segoe UI", "PingFang SC", "Microsoft YaHei", system-ui, sans-serif;
  overflow: hidden;
  transition: opacity 0.3s var(--ease);
}
.login-page.leaving { opacity: 0; }

.ambient {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}
.ambient-grid {
  position: absolute;
  inset: -12%;
  background-image:
    linear-gradient(rgba(17, 24, 39, 0.028) 1px, transparent 1px),
    linear-gradient(90deg, rgba(17, 24, 39, 0.028) 1px, transparent 1px);
  background-size: 40px 40px;
  opacity: 0.55;
  animation: grid-breathe 36s ease-in-out infinite;
}
.ambient-spotlight {
  position: absolute;
  inset: 0;
  transition: background 0.15s ease-out;
}
.ambient-glow {
  position: absolute;
  width: 70vw;
  height: 70vw;
  max-width: 820px;
  max-height: 820px;
  border-radius: 50%;
  filter: blur(120px);
  will-change: transform;
}
.ambient-glow--a {
  background: #16a34a;
  top: -22%;
  left: -8%;
  opacity: 0.09;
  animation: glow-float-a 36s ease-in-out infinite;
}
.ambient-glow--b {
  background: #86efac;
  bottom: -28%;
  right: -10%;
  opacity: 0.07;
  animation: glow-float-b 40s ease-in-out infinite;
}

.knowledge-float {
  position: absolute;
  inset: 0;
}
.kf {
  position: absolute;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.02em;
  color: var(--text);
  opacity: 0.03;
  animation-name: kf-drift;
  animation-timing-function: ease-in-out;
  animation-iteration-count: infinite;
  user-select: none;
}
.kf :deep(svg) { flex-shrink: 0; }

@keyframes grid-breathe {
  0%, 100% { transform: translate3d(0, 0, 0); opacity: 0.45; }
  50% { transform: translate3d(12px, 8px, 0); opacity: 0.65; }
}
@keyframes glow-float-a {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  33% { transform: translate3d(48px, 36px, 0) scale(1.06); }
  66% { transform: translate3d(-28px, 56px, 0) scale(0.96); }
}
@keyframes glow-float-b {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  40% { transform: translate3d(-56px, -40px, 0) scale(1.08); }
  70% { transform: translate3d(32px, -24px, 0) scale(0.94); }
}
@keyframes kf-drift {
  0%, 100% { transform: translate3d(0, 0, 0); }
  50% { transform: translate3d(14px, -18px, 0); }
}

.brand,
.panel {
  position: relative;
  z-index: 1;
  min-height: 100vh;
}

.brand {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px 7% 40px 8%;
}
.brand-inner {
  width: 100%;
  max-width: 480px;
  will-change: transform;
  transition: transform 0.05s linear;
}

.workspace-meta {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 28px;
  font-size: 13px;
  font-weight: 450;
  color: var(--muted);
  letter-spacing: 0.01em;
}
.greeting {
  color: var(--text);
  font-weight: 500;
}
.clock-dot {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: color-mix(in srgb, var(--muted) 55%, transparent);
}
.clock {
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.04em;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 36px;
}
.logo-mark {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.72);
  color: var(--brand);
  display: grid;
  place-items: center;
  backdrop-filter: blur(8px);
  transition: transform 0.15s var(--ease), box-shadow 0.15s var(--ease), color 0.15s var(--ease);
}
.logo:hover .logo-mark {
  transform: translateY(-2px);
  color: var(--brand-deep);
  box-shadow: 0 6px 14px rgba(22, 163, 74, 0.12);
}
.logo-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.logo-name {
  font-size: 15px;
  font-weight: 560;
  letter-spacing: -0.02em;
}
.logo-os {
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--muted);
  font-weight: 500;
}

.brand-title {
  margin: 0 0 14px;
  font-size: clamp(2rem, 2.8vw, 2.5rem);
  font-weight: 500;
  letter-spacing: -0.035em;
  line-height: 1.15;
}
.brand-desc {
  margin: 0 0 28px;
  font-size: 14px;
  line-height: 1.7;
  color: var(--muted);
  font-weight: 400;
}

.features {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 20px;
  max-width: 320px;
}
.feature {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 450;
  color: var(--text);
  padding: 8px 10px;
  margin: -8px -10px;
  border-radius: 10px;
  cursor: default;
  transition: background 0.15s var(--ease), transform 0.15s var(--ease);
}
.feature :deep(svg) {
  color: var(--brand);
  transition: transform 0.15s var(--ease);
}
.feature:hover {
  background: rgba(22, 163, 74, 0.06);
  transform: translateY(-2px);
}
.feature:hover :deep(svg) {
  transform: scale(1.05);
}

.quote {
  margin: 0;
  padding: 0;
  border: none;
  max-width: 42ch;
}
.quote-label {
  display: block;
  margin-bottom: 8px;
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #9ca3af;
  font-style: normal;
}
.quote-text {
  margin: 0;
  font-size: 13px;
  line-height: 1.65;
  color: #9ca3af;
  font-style: italic;
}

.panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 32px;
}

.card {
  width: min(440px, 100%);
  padding: 36px 32px 24px;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.65);
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(20px) saturate(1.2);
  -webkit-backdrop-filter: blur(20px) saturate(1.2);
  box-shadow:
    0 1px 1px rgba(17, 24, 39, 0.02),
    0 12px 32px rgba(17, 24, 39, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.75);
  will-change: transform;
  transition:
    transform 0.05s linear,
    box-shadow 0.15s var(--ease),
    border-color 0.15s var(--ease);
}
.card:hover {
  border-color: rgba(255, 255, 255, 0.85);
  box-shadow:
    0 1px 1px rgba(17, 24, 39, 0.02),
    0 18px 44px rgba(17, 24, 39, 0.07),
    inset 0 1px 0 rgba(255, 255, 255, 0.85);
}

.card-head { margin-bottom: 28px; position: relative; }
.back-btn {
  border: none;
  background: none;
  padding: 0;
  margin: 0 0 10px;
  font-size: 13px;
  color: var(--muted);
  cursor: pointer;
  transition: color 0.15s var(--ease);
}
.back-btn:hover { color: var(--brand); }

.step-pane { width: 100%; }
.step-pane--center {
  min-height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  padding: 24px 0;
}
.loading-spinner {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid color-mix(in srgb, var(--brand) 25%, transparent);
  border-top-color: var(--brand);
  animation: spin-load 0.7s linear infinite;
}
.loading-text {
  margin: 0;
  font-size: 14px;
  color: var(--muted);
  font-weight: 450;
}
@keyframes spin-load { to { transform: rotate(360deg); } }

.verify-hint {
  margin: 14px 0 0;
  text-align: center;
  font-size: 12px;
  color: var(--muted);
}
.verify-hint.ok { color: var(--brand); font-weight: 500; }

.step-enter-active,
.step-leave-active {
  transition: opacity 0.22s var(--ease), transform 0.22s var(--ease);
}
.step-enter-from {
  opacity: 0;
  transform: translateY(14px);
}
.step-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.card-title {
  margin: 0 0 8px;
  font-size: 1.35rem;
  font-weight: 500;
  letter-spacing: -0.02em;
}
.card-sub {
  margin: 0;
  font-size: 14px;
  font-weight: 400;
  color: var(--muted);
}

.form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.field-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--muted);
}
.field-shell {
  position: relative;
  display: flex;
  align-items: center;
}
.field-icon {
  position: absolute;
  left: 14px;
  color: var(--muted);
  pointer-events: none;
  transition: color 0.15s var(--ease);
}
.field-input {
  width: 100%;
  height: 48px;
  padding: 0 14px 0 40px;
  border: 1px solid color-mix(in srgb, var(--line) 90%, transparent);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.78);
  font-size: 14px;
  font-weight: 400;
  color: var(--text);
  outline: none;
  transition:
    border-color 0.15s var(--ease),
    box-shadow 0.15s var(--ease),
    background 0.15s var(--ease);
}
.field-input::placeholder { color: #9ca3af; }
.field-input:hover {
  border-color: color-mix(in srgb, var(--brand) 22%, var(--line));
}
.field-input:focus {
  border-color: var(--brand);
  box-shadow: 0 0 0 4px rgba(22, 163, 74, 0.14);
  background: #fff;
}
.field-shell:focus-within .field-icon { color: var(--brand); }

.submit {
  position: relative;
  overflow: hidden;
  margin-top: 4px;
  height: 50px;
  border: none;
  border-radius: 12px;
  background: var(--brand);
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition:
    background 0.15s var(--ease),
    transform 0.1s var(--ease),
    box-shadow 0.15s var(--ease),
    opacity 0.15s var(--ease);
}
.submit:hover:not(:disabled) {
  background: var(--brand-deep);
  transform: translateY(-2px);
  box-shadow: 0 10px 22px rgba(22, 163, 74, 0.22);
}
.submit:active:not(:disabled) {
  transform: scale(0.98);
  box-shadow: 0 4px 12px rgba(22, 163, 74, 0.16);
}
.submit:disabled {
  opacity: 0.45;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.remember {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--muted);
  cursor: pointer;
  user-select: none;
}
.remember input {
  width: 14px;
  height: 14px;
  accent-color: var(--brand);
  cursor: pointer;
}
.ghost-link {
  border: none;
  background: none;
  padding: 0;
  font-size: 13px;
  color: var(--muted);
  cursor: pointer;
  transition: color 0.15s var(--ease);
}
.ghost-link:hover { color: var(--brand); }

/* Fade + Up · 200~250ms */
.anim {
  animation: rise-in 0.24s var(--ease) both;
}
.anim-1 { animation-delay: 0.02s; }
.anim-2 { animation-delay: 0.05s; }
.anim-3 { animation-delay: 0.08s; }
.anim-4 { animation-delay: 0.11s; }
.anim-5 { animation-delay: 0.14s; }
.anim-6 { animation-delay: 0.18s; }
.anim-card {
  animation: rise-in 0.25s var(--ease) both;
  animation-delay: 0.06s;
}

@keyframes rise-in {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 1200px) {
  .login-page { grid-template-columns: 1fr; }
  .brand { display: none; }
  .panel { min-height: 100vh; padding: 32px 20px; }
  .knowledge-float { display: none; }
}

@media (max-width: 480px) {
  .card {
    padding: 28px 18px 20px;
    background: rgba(255, 255, 255, 0.88);
  }
}

@media (prefers-reduced-motion: reduce) {
  .ambient-grid,
  .ambient-glow,
  .kf,
  .anim,
  .anim-card {
    animation: none !important;
  }
}
</style>
