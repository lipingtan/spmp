<!--
  H5 个人信息页面
  - 展示个人信息：头像（van-image 圆形）、姓名、手机号（脱敏）、身份证号（脱敏）、性别、邮箱、业主状态（van-tag 颜色区分）
  - 展示已绑定房产数量（van-cell，点击跳转到我的房产页面）
  - 数据通过 getProfile API 获取
-->
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProfile } from '@/api/owner'
import type { H5ProfileVO } from '@/api/owner'

const router = useRouter()

// ==================== 状态 ====================

/** 个人信息 */
const profile = ref<H5ProfileVO | null>(null)
/** 加载状态 */
const loading = ref(false)

// ==================== 计算属性 ====================

/** 已绑定房产数量 */
const propertyCount = computed(() => profile.value?.propertyBindings?.length ?? 0)

// ==================== 性别映射 ====================

/** 性别枚举 → 中文文本 */
function genderText(gender: number): string {
  const map: Record<number, string> = { 0: '未知', 1: '男', 2: '女' }
  return map[gender] ?? '未知'
}

// ==================== 业主状态映射 ====================

/** 业主状态 → 中文文本 */
function statusText(status: string): string {
  const map: Record<string, string> = {
    UNCERTIFIED: '未认证',
    CERTIFYING: '认证中',
    CERTIFIED: '已认证',
    DISABLED: '已停用'
  }
  return map[status] ?? status
}

/** 业主状态 → 标签类型 */
function statusTagType(status: string): 'default' | 'warning' | 'success' | 'danger' {
  const map: Record<string, 'default' | 'warning' | 'success' | 'danger'> = {
    UNCERTIFIED: 'default',
    CERTIFYING: 'warning',
    CERTIFIED: 'success',
    DISABLED: 'danger'
  }
  return map[status] ?? 'default'
}

// ==================== 数据加载 ====================

/** 加载个人信息 */
async function fetchProfile() {
  loading.value = true
  try {
    profile.value = await getProfile()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// ==================== 导航 ====================

/** 跳转到我的房产页面 */
function goToProperties() {
  router.push('/mine/properties')
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchProfile()
})
</script>

<template>
  <div class="profile-page">
    <!-- 顶部导航栏 -->
    <van-nav-bar title="个人信息" left-arrow @click-left="$router.back()" />

    <!-- 加载状态 -->
    <div v-if="loading" class="page-loading">
      <van-loading size="24px">加载中...</van-loading>
    </div>

    <!-- 个人信息内容 -->
    <template v-else-if="profile">
      <!-- 头像区域 -->
      <div class="avatar-section">
        <van-image
          round
          width="72px"
          height="72px"
          :src="profile.avatar"
          fit="cover"
        >
          <template #error>
            <van-icon name="user-o" size="36" color="#c8c9cc" />
          </template>
        </van-image>
        <div class="avatar-name">{{ profile.name }}</div>
        <van-tag :type="statusTagType(profile.status)" size="medium">
          {{ statusText(profile.status) }}
        </van-tag>
      </div>

      <!-- 基本信息 -->
      <van-cell-group inset title="基本信息" class="info-group">
        <van-cell title="姓名" :value="profile.name" />
        <van-cell title="手机号" :value="profile.phoneMasked" />
        <van-cell title="身份证号" :value="profile.idCardMasked" />
        <van-cell title="性别" :value="genderText(profile.gender)" />
        <van-cell title="邮箱" :value="profile.email || '未设置'" />
      </van-cell-group>

      <!-- 房产信息 -->
      <van-cell-group inset class="info-group">
        <van-cell
          title="已绑定房产"
          :value="`${propertyCount} 套`"
          is-link
          @click="goToProperties"
        />
      </van-cell-group>
    </template>
  </div>
</template>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--color-bg-page);
}

.page-loading {
  display: flex;
  justify-content: center;
  padding: var(--spacing-xxl) 0;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-xl) 0;
  gap: 8px;
}

.avatar-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.info-group {
  margin-top: var(--spacing-md);
}
</style>
