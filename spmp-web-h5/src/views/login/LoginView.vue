<!--
  H5 业主端登录页
  - 支持手机号密码登录（带图形验证码）
  - 支持手机号短信验证码登录
  - 登录成功后跳转 redirect 或首页
-->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import { getCaptcha, login, loginBySms, sendSmsCode } from '@/api/auth'
import type { CaptchaResult } from '@/api/auth'
import { useUserStore } from '@/store/modules/user'
import { isRepairRole } from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// ==================== 登录方式切换 ====================
type LoginMode = 'password' | 'sms'
const loginMode = ref<LoginMode>('password')

// ==================== 密码登录表单 ====================
const pwdForm = ref({
  username: '',
  password: '',
  captchaCode: '',
  captchaKey: ''
})
const captcha = ref<CaptchaResult | null>(null)
const pwdLoading = ref(false)

// ==================== 短信登录表单 ====================
const smsForm = ref({
  phone: '',
  smsCode: ''
})
const smsLoading = ref(false)
const smsSending = ref(false)
const smsCountdown = ref(0)
let countdownTimer: ReturnType<typeof setInterval> | null = null

// ==================== 验证码 ====================

async function loadCaptcha() {
  try {
    captcha.value = await getCaptcha()
    pwdForm.value.captchaKey = captcha.value.captchaKey
    pwdForm.value.captchaCode = ''
  } catch {
    // 忽略
  }
}

// ==================== 密码登录 ====================

async function handlePwdLogin() {
  if (!pwdForm.value.username) {
    showToast('请输入用户名')
    return
  }
  if (!pwdForm.value.password) {
    showToast('请输入密码')
    return
  }
  if (!pwdForm.value.captchaCode) {
    showToast('请输入验证码')
    return
  }

  pwdLoading.value = true
  try {
    const result = await login({
      username: pwdForm.value.username,
      password: pwdForm.value.password,
      captchaCode: pwdForm.value.captchaCode,
      captchaKey: pwdForm.value.captchaKey,
      clientType: 'owner'
    })
    await saveTokenAndRedirect(result.accessToken, result.username)
  } catch {
    // 登录失败刷新验证码
    await loadCaptcha()
  } finally {
    pwdLoading.value = false
  }
}

// ==================== 短信登录 ====================

async function handleSendSms() {
  if (!smsForm.value.phone) {
    showToast('请输入手机号')
    return
  }
  if (smsCountdown.value > 0) return

  smsSending.value = true
  try {
    await sendSmsCode(smsForm.value.phone)
    showToast('验证码已发送')
    startCountdown()
  } catch {
    // 错误已在拦截器处理
  } finally {
    smsSending.value = false
  }
}

function startCountdown() {
  smsCountdown.value = 60
  countdownTimer = setInterval(() => {
    smsCountdown.value--
    if (smsCountdown.value <= 0) {
      clearInterval(countdownTimer!)
      countdownTimer = null
    }
  }, 1000)
}

async function handleSmsLogin() {
  if (!smsForm.value.phone) {
    showToast('请输入手机号')
    return
  }
  if (!smsForm.value.smsCode) {
    showToast('请输入验证码')
    return
  }

  smsLoading.value = true
  try {
    const result = await loginBySms({
      phone: smsForm.value.phone,
      smsCode: smsForm.value.smsCode,
      clientType: 'owner'
    })
    await saveTokenAndRedirect(result.accessToken, result.username)
  } catch {
    // 错误已在拦截器处理
  } finally {
    smsLoading.value = false
  }
}

// ==================== 工具方法 ====================

async function saveTokenAndRedirect(token: string, username: string) {
  localStorage.setItem('access_token', token)
  localStorage.setItem('h5_username', username)
  // 同步更新 store，确保 MineView 等页面的登录状态立即生效
  userStore.token = token
  userStore.username = username
  showToast({ message: '登录成功', type: 'success' })
  const redirect = route.query.redirect as string | undefined
  if (redirect) {
    router.replace(redirect)
    return
  }
  const redirectPath = isRepairRole(token) ? '/repair/dashboard' : '/home'
  router.replace(redirectPath)
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadCaptcha()
})
</script>

<template>
  <div class="login-page">
    <div class="login-bg-glow"></div>
    <div class="login-bg-grid"></div>
    <!-- 顶部 Logo 区域 -->
    <div class="login-header">
      <div class="logo-icon">🏠</div>
      <div class="app-name">智慧物业</div>
      <div class="app-desc">Smart Property Experience</div>
    </div>

    <!-- 登录方式切换 -->
    <div class="mode-tabs">
      <span
        class="mode-tab"
        :class="{ active: loginMode === 'password' }"
        @click="loginMode = 'password'"
      >密码登录</span>
      <span
        class="mode-tab"
        :class="{ active: loginMode === 'sms' }"
        @click="loginMode = 'sms'"
      >短信登录</span>
    </div>

    <!-- 密码登录表单 -->
    <van-form v-if="loginMode === 'password'" @submit="handlePwdLogin" class="login-form">
      <van-cell-group inset>
        <van-field
          v-model="pwdForm.username"
          name="username"
          label="用户名"
          placeholder="请输入用户名"
          :rules="[{ required: true, message: '请输入用户名' }]"
        />
        <van-field
          v-model="pwdForm.password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          type="password"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
        <van-field
          v-model="pwdForm.captchaCode"
          name="captchaCode"
          label="验证码"
          placeholder="请输入验证码"
          :rules="[{ required: true, message: '请输入验证码' }]"
        >
          <template #button>
            <img
              v-if="captcha"
              :src="captcha.captchaImage"
              class="captcha-img"
              alt="验证码"
              @click="loadCaptcha"
            />
          </template>
        </van-field>
      </van-cell-group>

      <div class="form-actions">
        <van-button
          type="primary"
          block
          round
          native-type="submit"
          :loading="pwdLoading"
          loading-text="登录中..."
        >
          登录
        </van-button>
      </div>
    </van-form>

    <!-- 短信登录表单 -->
    <van-form v-else @submit="handleSmsLogin" class="login-form">
      <van-cell-group inset>
        <van-field
          v-model="smsForm.phone"
          name="phone"
          label="手机号"
          placeholder="请输入手机号"
          type="tel"
          :rules="[{ required: true, message: '请输入手机号' }]"
        />
        <van-field
          v-model="smsForm.smsCode"
          name="smsCode"
          label="验证码"
          placeholder="请输入短信验证码"
          :rules="[{ required: true, message: '请输入验证码' }]"
        >
          <template #button>
            <van-button
              size="small"
              type="primary"
              :disabled="smsCountdown > 0 || smsSending"
              :loading="smsSending"
              @click.prevent="handleSendSms"
            >
              {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
            </van-button>
          </template>
        </van-field>
      </van-cell-group>

      <div class="form-actions">
        <van-button
          type="primary"
          block
          round
          native-type="submit"
          :loading="smsLoading"
          loading-text="登录中..."
        >
          登录
        </van-button>
      </div>
    </van-form>

    <!-- 底部注册入口 -->
    <div class="login-footer">
      <span>还没有账号？</span>
      <van-button type="default" size="small" plain @click="$router.push('/register')">
        立即注册
      </van-button>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  position: relative;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background:
    linear-gradient(145deg, rgba(44, 101, 255, 0.14), rgba(0, 0, 0, 0) 52%),
    linear-gradient(330deg, rgba(212, 175, 55, 0.14), rgba(0, 0, 0, 0) 48%),
    var(--color-bg-page);
  padding-bottom: 40px;
  overflow: hidden;
}

.login-bg-glow {
  position: absolute;
  width: 380px;
  height: 380px;
  right: -160px;
  top: -120px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(212, 175, 55, 0.22), transparent 68%);
  filter: blur(10px);
  animation: shimmer 10s ease-in-out infinite;
}

.login-bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(to right, rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 28px 28px;
  mask-image: radial-gradient(circle at 50% 30%, black 20%, transparent 90%);
  pointer-events: none;
}

.login-header {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 68px 0 36px;
}

.logo-icon {
  width: 84px;
  height: 84px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 24px;
  font-size: 42px;
  margin-bottom: 14px;
  background: linear-gradient(145deg, var(--glass-bg), rgba(255, 255, 255, 0.08));
  border: 1px solid var(--glass-border);
  box-shadow: var(--shadow-md);
  backdrop-filter: blur(14px);
}

.app-name {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 6px;
  letter-spacing: 1px;
  text-shadow: var(--tabbar-active-shadow);
}

.app-desc {
  font-size: 12px;
  color: var(--color-text-secondary);
  letter-spacing: 1.6px;
  text-transform: uppercase;
}

.mode-tabs {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: center;
  gap: 22px;
  margin-bottom: 20px;
}

.mode-tab {
  font-size: 14px;
  color: var(--color-text-secondary);
  padding: 8px 16px;
  border-radius: 999px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.mode-tab.active {
  color: var(--color-text-primary);
  font-weight: 600;
  border-color: var(--glass-border);
  background: var(--glass-bg);
  box-shadow: var(--shadow-sm);
}

.login-form {
  position: relative;
  z-index: 1;
  padding: 0 8px;
}

.login-form :deep(.van-cell-group) {
  border-radius: 18px;
  overflow: hidden;
  border: 1px solid var(--glass-border);
  box-shadow: var(--shadow-md);
  backdrop-filter: blur(16px);
}

.captcha-img {
  height: 32px;
  cursor: pointer;
  border-radius: 8px;
  border: 1px solid var(--glass-border);
}

.form-actions {
  padding: 24px 16px 0;
}

.login-footer {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 24px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

@keyframes shimmer {
  0%, 100% {
    transform: translate3d(0, 0, 0) scale(1);
    opacity: 0.8;
  }
  50% {
    transform: translate3d(-24px, 12px, 0) scale(1.08);
    opacity: 1;
  }
}
</style>
