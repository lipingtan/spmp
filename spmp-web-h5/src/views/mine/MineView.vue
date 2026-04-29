<!--
  我的 Tab 页面
  - 未登录：显示登录/注册入口
  - 已登录：显示用户头像+姓名、功能入口列表
-->
<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { getProfile } from '@/api/owner'
import { useAppStore } from '@/store/modules/app'
import type { ActionSheetAction } from 'vant'
import { isRepairRole } from '@/utils/auth'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

/** 是否已登录 */
const isLoggedIn = computed(() => !!userStore.token)

const menuItems = ref<{ title: string; icon: string; path: string }[]>([
  { title: '个人信息', icon: 'user-o', path: '/mine/profile' }
])
const showThemeSheet = ref(false)
const themeActions = computed<ActionSheetAction[]>(() => ([
  { name: '商务蓝（经典）', color: appStore.themeMode === 'business' ? 'var(--color-primary)' : undefined },
  { name: '暗夜奢华（高端）', color: appStore.themeMode === 'luxury-dark' ? 'var(--color-primary)' : undefined }
]))

async function loadMenus() {
  if (!isLoggedIn.value) {
    menuItems.value = [{ title: '个人信息', icon: 'user-o', path: '/mine/profile' }]
    return
  }
  const hasRepairAccess = isRepairRole(userStore.token)
  try {
    const profile = await getProfile()
    const hasProperties = (profile.propertyBindings?.length || 0) > 0
    const nextMenus = [{ title: '个人信息', icon: 'user-o', path: '/mine/profile' }]
    if (hasProperties) {
      nextMenus.push(
        { title: '我的房产', icon: 'wap-home-o', path: '/mine/properties' },
        { title: '家庭成员', icon: 'friends-o', path: '/mine/family' }
      )
    }
    if (profile.status !== 'CERTIFIED') {
      nextMenus.push({ title: '房产认证', icon: 'certificate', path: '/certify' })
    }
    if (hasRepairAccess) {
      nextMenus.push({ title: '维修工作台', icon: 'orders-o', path: '/repair/dashboard' })
    }
    menuItems.value = nextMenus
  } catch {
    const fallbackMenus = [
      { title: '个人信息', icon: 'user-o', path: '/mine/profile' },
      { title: '我的房产', icon: 'wap-home-o', path: '/mine/properties' },
      { title: '家庭成员', icon: 'friends-o', path: '/mine/family' },
      { title: '房产认证', icon: 'certificate', path: '/certify' }
    ]
    if (hasRepairAccess) {
      fallbackMenus.push({ title: '维修工作台', icon: 'orders-o', path: '/repair/dashboard' })
    }
    menuItems.value = fallbackMenus
  }
}

onMounted(() => {
  loadMenus()
})

function handleThemeSelect(action: ActionSheetAction) {
  if (action.name === '暗夜奢华（高端）') {
    appStore.applyTheme('luxury-dark')
  } else {
    appStore.applyTheme('business')
  }
  showThemeSheet.value = false
}
</script>

<template>
  <div class="mine-page">
    <!-- 未登录状态 -->
    <div v-if="!isLoggedIn" class="mine-unlogged">
      <van-icon name="user-o" size="64" color="#c8c9cc" />
      <p class="unlogged-text">登录后享受更多服务</p>
      <div class="unlogged-actions">
        <van-button type="primary" round size="small" @click="router.push('/login')">登录</van-button>
        <van-button round size="small" @click="router.push('/register')">注册</van-button>
      </div>
    </div>

    <!-- 已登录状态 -->
    <template v-else>
      <!-- 用户信息卡片 -->
      <div class="mine-header" @click="router.push('/mine/profile')">
        <van-image round width="56px" height="56px" :src="userStore.avatar">
          <template #error>
            <van-icon name="user-o" size="28" color="#c8c9cc" />
          </template>
        </van-image>
        <div class="mine-header__info">
          <span class="mine-header__name">{{ userStore.username || '业主' }}</span>
          <span class="mine-header__tip">点击查看个人信息</span>
        </div>
        <van-icon name="arrow" />
      </div>

      <!-- 功能入口 -->
      <van-cell-group inset class="mine-menu">
        <van-cell
          title="界面风格"
          icon="brush-o"
          :value="appStore.themeMode === 'luxury-dark' ? '暗夜奢华' : '商务蓝'"
          is-link
          @click="showThemeSheet = true"
        />
        <van-cell
          v-for="item in menuItems"
          :key="item.path"
          :title="item.title"
          :icon="item.icon"
          is-link
          @click="router.push(item.path)"
        />
      </van-cell-group>

      <!-- 退出登录 -->
      <div class="mine-logout">
        <van-button block round plain type="danger" @click="userStore.logout()">退出登录</van-button>
      </div>

      <van-action-sheet
        v-model:show="showThemeSheet"
        :actions="themeActions"
        cancel-text="取消"
        close-on-click-action
        @select="handleThemeSelect"
      />
    </template>
  </div>
</template>

<style scoped>
.mine-page {
  min-height: 80vh;
  background-color: var(--color-bg-page);
}

.mine-unlogged {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0 40px;
  gap: 12px;
}

.unlogged-text {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.unlogged-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.mine-header {
  display: flex;
  align-items: center;
  padding: 24px 16px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-hover));
  color: #fff;
  gap: 12px;
}

.mine-header__info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.mine-header__name {
  font-size: 18px;
  font-weight: 600;
}

.mine-header__tip {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 4px;
}

.mine-menu {
  margin-top: 12px;
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.mine-logout {
  padding: 24px 16px;
}
</style>
