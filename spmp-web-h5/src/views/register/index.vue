<!--
  H5 注册页面
  - van-form + van-field 输入手机号/密码/姓名/身份证号
  - 表单校验：手机号正则、密码长度 6-20 位
  - 调用 register API，成功后跳转登录页
  - 底部"已有账号？去登录"链接
-->
<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import type { FormInstance } from 'vant'
import { register } from '@/api/owner'
import type { H5RegisterDTO } from '@/api/owner'

const router = useRouter()

/** 表单引用 */
const formRef = ref<FormInstance>()

/** 表单数据 */
const formData = reactive<H5RegisterDTO>({
  phone: '',
  password: '',
  ownerName: '',
  idCard: '',
  captcha: ''
})

/** 提交加载状态 */
const loading = ref(false)

/** 手机号校验规则 */
const phonePattern = /^1[3-9]\d{9}$/

/** 表单校验规则 */
const phoneRules = [
  { required: true, message: '请输入手机号' },
  { pattern: phonePattern, message: '手机号格式不正确' }
]
const passwordRules = [
  { required: true, message: '请输入密码' },
  { validator: (val: string) => val.length >= 6 && val.length <= 20, message: '密码长度为 6-20 位' }
]
const nameRules = [
  { required: true, message: '请输入姓名' }
]
const captchaRules = [
  { required: true, message: '请输入验证码' }
]

/** 提交注册 */
async function handleRegister() {
  try {
    // 触发表单校验
    await formRef.value?.validate()
  } catch {
    // 校验不通过，不继续
    return
  }

  loading.value = true
  try {
    await register(formData)
    showToast({ message: '注册成功', type: 'success' })
    // 注册成功后跳转登录页
    router.replace('/login')
  } catch {
    // 错误已在 axios 拦截器中统一处理
  } finally {
    loading.value = false
  }
}

/** 跳转登录页 */
function goLogin() {
  router.push('/login')
}
</script>

<template>
  <div class="register-page">
    <!-- 顶部导航栏 -->
    <van-nav-bar title="注册" left-arrow @click-left="goLogin" />

    <div class="register-header">
      <h1 class="register-title">智慧物业</h1>
      <p class="register-subtitle">业主注册</p>
    </div>

    <!-- 注册表单 -->
    <div class="register-form">
      <van-form ref="formRef" @submit="handleRegister">
        <van-cell-group inset>
          <van-field
            v-model="formData.phone"
            name="phone"
            label="手机号"
            placeholder="请输入手机号"
            left-icon="phone-o"
            type="tel"
            maxlength="11"
            clearable
            :rules="phoneRules"
          />
          <van-field
            v-model="formData.password"
            name="password"
            label="密码"
            placeholder="请输入密码（6-20位）"
            left-icon="lock"
            type="password"
            maxlength="20"
            clearable
            :rules="passwordRules"
          />
          <van-field
            v-model="formData.ownerName"
            name="ownerName"
            label="姓名"
            placeholder="请输入姓名"
            left-icon="user-o"
            clearable
            :rules="nameRules"
          />
          <van-field
            v-model="formData.idCard"
            name="idCard"
            label="身份证号"
            placeholder="请输入身份证号（选填）"
            left-icon="idcard"
            maxlength="18"
            clearable
          />
          <van-field
            v-model="formData.captcha"
            name="captcha"
            label="验证码"
            placeholder="请输入验证码"
            left-icon="shield-o"
            maxlength="6"
            clearable
            :rules="captchaRules"
          >
            <template #extra>
              <span style="font-size: 12px; color: #999;">演示验证码：123456</span>
            </template>
          </van-field>
        </van-cell-group>

        <!-- 注册按钮 -->
        <div class="register-action">
          <van-button
            type="primary"
            block
            round
            native-type="submit"
            :loading="loading"
            loading-text="注册中..."
          >
            注册
          </van-button>
        </div>
      </van-form>
    </div>

    <!-- 底部登录链接 -->
    <div class="register-footer">
      <span class="footer-text">已有账号？</span>
      <span class="footer-link" @click="goLogin">去登录</span>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--color-bg-page);
}

.register-header {
  text-align: center;
  padding: var(--spacing-xxl) 0 var(--spacing-xl);
}

.register-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: var(--spacing-xs);
}

.register-subtitle {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.register-form {
  padding: 0 var(--spacing-lg);
}

.register-action {
  padding: var(--spacing-xl) var(--spacing-lg) 0;
}

.register-footer {
  text-align: center;
  padding: var(--spacing-xl) 0;
}

.footer-text {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.footer-link {
  font-size: 14px;
  color: var(--color-primary);
  cursor: pointer;
}
</style>
