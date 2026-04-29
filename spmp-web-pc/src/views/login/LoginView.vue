<!--
  登录页
  - 居中卡片布局，标题「智慧物业管理平台」
  - el-form + el-input(用户名/密码/验证码) + el-button
  - 表单校验(非空)，调用 userStore.login()
  - 成功后 router.replace(redirect || '/home')
-->
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const captchaImg = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  captchaCode: '',
  captchaKey: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

/** 加载验证码 */
async function loadCaptcha() {
  try {
    const res = await userStore.fetchCaptcha()
    captchaImg.value = res.captchaImage
    loginForm.captchaKey = res.captchaKey
  } catch {
    // 验证码加载失败不阻塞
  }
}

/** 提交登录 */
async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login({
      username: loginForm.username,
      password: loginForm.password,
      captchaCode: loginForm.captchaCode,
      captchaKey: loginForm.captchaKey,
      clientType: 'admin'
    })
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/home'
    router.replace(redirect)
  } catch {
    ElMessage.error('登录失败，请重试')
    loadCaptcha()
    loginForm.captchaCode = ''
  } finally {
    loading.value = false
  }
}

onMounted(loadCaptcha)
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="login-title">智慧物业管理平台</h1>
      <p class="login-subtitle">PC 管理端</p>

      <el-form ref="formRef" :model="loginForm" :rules="rules" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model="loginForm.captchaCode" placeholder="请输入验证码" style="flex: 1" />
            <img v-if="captchaImg" :src="captchaImg" class="captcha-img" alt="验证码" @click="loadCaptcha" />
            <el-button v-else class="captcha-placeholder" @click="loadCaptcha">获取验证码</el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: var(--color-bg-page);
}
.login-card {
  width: 400px;
  padding: 40px;
  background-color: var(--color-bg-white);
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-lg);
}
.login-title {
  text-align: center;
  font-size: 24px;
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: var(--spacing-sm);
}
.login-subtitle {
  text-align: center;
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-xxl);
}
.login-btn { width: 100%; }
.captcha-row {
  display: flex;
  gap: 8px;
  width: 100%;
}
.captcha-img {
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}
.captcha-placeholder {
  height: 40px;
  white-space: nowrap;
}
</style>
