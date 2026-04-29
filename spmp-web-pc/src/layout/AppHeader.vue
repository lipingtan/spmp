<!--
  顶部导航栏
  - 64px 高，白色背景 + 底部边框
  - 左侧：Logo + 系统名称 + 折叠按钮
  - 右侧：用户头像 + 下拉菜单（退出登录）
-->
<script setup lang="ts">
import { useAppStore } from '@/store/modules/app'
import { useUserStore } from '@/store/modules/user'
import { Fold, Expand, UserFilled, Brush } from '@element-plus/icons-vue'

const appStore = useAppStore()
const userStore = useUserStore()

/** 退出登录 */
function handleLogout() {
  userStore.logout()
}

function switchTheme(mode: 'business' | 'luxury-dark' | 'aurora-local') {
  appStore.applyTheme(mode)
}
</script>

<template>
  <el-header class="app-header" :class="{ 'aurora-local': appStore.themeMode === 'aurora-local' }">
    <!-- 左侧区域 -->
    <div class="app-header__left">
      <div class="app-header__logo">
        <span class="app-header__title">智慧物业管理平台</span>
      </div>
      <el-icon
        class="app-header__collapse-btn"
        @click="appStore.toggleSidebar()"
      >
        <Fold v-if="!appStore.sidebarCollapsed" />
        <Expand v-else />
      </el-icon>
    </div>

    <!-- 右侧区域 -->
    <div class="app-header__right">
      <el-dropdown trigger="click">
        <el-button class="theme-btn" text>
          <el-icon><Brush /></el-icon>
          <span>
            {{
              appStore.themeMode === 'luxury-dark'
                ? '暗夜奢华'
                : appStore.themeMode === 'aurora-local'
                  ? '极光科技'
                  : '商务经典'
            }}
          </span>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="switchTheme('business')">商务经典</el-dropdown-item>
            <el-dropdown-item @click="switchTheme('luxury-dark')">暗夜奢华</el-dropdown-item>
            <el-dropdown-item @click="switchTheme('aurora-local')">极光科技（布局专属）</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <el-dropdown trigger="click" @command="handleLogout">
        <div class="app-header__user">
          <el-avatar :size="32" :icon="UserFilled" />
          <span class="app-header__username">{{ userStore.username || '用户' }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<style scoped>
.app-header {
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-xl);
  background: var(--glass-bg);
  border-bottom: 1px solid var(--glass-border);
  backdrop-filter: blur(12px);
  box-shadow: var(--shadow-sm);
  z-index: 1000;
}

.app-header__left {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.app-header__logo {
  display: flex;
  align-items: center;
}

.app-header__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-primary);
  white-space: nowrap;
}

.app-header__collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--color-text-regular);
  transition: color 0.2s;
}

.app-header__collapse-btn:hover {
  color: var(--color-primary);
}

.app-header__right {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.app-header__user {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  cursor: pointer;
}

.app-header__username {
  font-size: 14px;
  color: var(--color-text-regular);
}

.theme-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--color-text-regular);
}

.app-header.aurora-local {
  background:
    linear-gradient(105deg, rgba(8, 27, 58, 0.88), rgba(17, 46, 96, 0.84)),
    radial-gradient(120% 100% at 0% 0%, rgba(52, 212, 255, 0.2), transparent 60%);
  border-bottom-color: rgba(86, 208, 255, 0.32);
  box-shadow: 0 12px 30px rgba(5, 20, 46, 0.34);
}

.app-header.aurora-local .app-header__title {
  color: #9de8ff;
  text-shadow: 0 0 16px rgba(92, 233, 255, 0.44);
}

.app-header.aurora-local .app-header__username,
.app-header.aurora-local .theme-btn,
.app-header.aurora-local .app-header__collapse-btn {
  color: #d3ecff;
}
</style>
