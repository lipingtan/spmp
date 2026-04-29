<!--
  侧边菜单栏
  - 默认 220px，折叠后 64px
  - el-menu router 模式，从路由 meta 提取菜单
  - 支持分组子菜单（系统管理、日志管理）
  - 高亮当前路由对应的菜单项
-->
<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store/modules/app'
import { asyncRoutes } from '@/router/static-routes'
import {
  HomeFilled, Setting, User, UserFilled, Tickets, Wallet, Bell, Lock,
  Menu, Document, Notebook, Avatar, Location, House, School, Key, OfficeBuilding, Stamp, DataAnalysis, Edit, Warning
} from '@element-plus/icons-vue'
import type { Component } from 'vue'

const route = useRoute()
const appStore = useAppStore()

const iconMap: Record<string, Component> = {
  HomeFilled, Setting, User, UserFilled, Tickets, Wallet, Bell, Lock,
  Menu, Document, Notebook, Avatar, Location, House, School, Key, OfficeBuilding, Stamp, DataAnalysis, Edit, Warning
}

/** 菜单分组定义 */
const menuGroups = computed(() => {
  const layoutRoute = asyncRoutes[0]
  if (!layoutRoute?.children) return []
  const children = layoutRoute.children.filter((r) => !r.meta?.hidden)

  // 分组：基础数据、业主管理、工单管理、公告管理、系统管理、日志管理、其他
  const baseItems = children.filter(r => r.path?.startsWith('base/'))
  const ownerItems = children.filter(r => r.path?.startsWith('owner/'))
  const workorderItems = children.filter(r => r.path?.startsWith('workorder/'))
  const billingItems = children.filter(r => r.path?.startsWith('billing/'))
  const noticeItems = children.filter(r => r.path?.startsWith('notice/'))
  const systemItems = children.filter(r => r.path?.startsWith('system/'))
  const logItems = children.filter(r => r.path?.startsWith('log/'))
  const otherItems = children.filter(r =>
    !r.path?.startsWith('system/') &&
    !r.path?.startsWith('log/') &&
    !r.path?.startsWith('base/') &&
    !r.path?.startsWith('owner/') &&
    !r.path?.startsWith('workorder/') &&
    !r.path?.startsWith('billing/') &&
    !r.path?.startsWith('notice/')
  )

  const groups: any[] = []
  otherItems.forEach(item => groups.push({ type: 'item', item }))
  if (baseItems.length) {
    groups.push({ type: 'submenu', title: '基础数据', icon: 'OfficeBuilding', index: 'base', children: baseItems })
  }
  if (ownerItems.length) {
    groups.push({ type: 'submenu', title: '业主管理', icon: 'UserFilled', index: 'owner', children: ownerItems })
  }
  if (workorderItems.length) {
    groups.push({ type: 'submenu', title: '工单管理', icon: 'Tickets', index: 'workorder', children: workorderItems })
  }
  if (billingItems.length) {
    groups.push({ type: 'submenu', title: '缴费管理', icon: 'Wallet', index: 'billing', children: billingItems })
  }
  if (noticeItems.length) {
    groups.push({ type: 'submenu', title: '公告管理', icon: 'Bell', index: 'notice', children: noticeItems })
  }
  // 系统管理子菜单
  if (systemItems.length) {
    groups.push({ type: 'submenu', title: '系统管理', icon: 'Setting', index: 'system', children: systemItems })
  }
  // 日志管理子菜单
  if (logItems.length) {
    groups.push({ type: 'submenu', title: '日志管理', icon: 'Document', index: 'log', children: logItems })
  }
  return groups
})

const activeMenu = computed(() => route.path)
</script>

<template>
  <el-aside class="app-sidebar" :class="{ 'aurora-local': appStore.themeMode === 'aurora-local' }" :width="appStore.sidebarCollapsed ? '64px' : '220px'">
    <el-menu :default-active="activeMenu" :collapse="appStore.sidebarCollapsed" :collapse-transition="false" router class="app-sidebar__menu">
      <template v-for="group in menuGroups" :key="group.type === 'item' ? group.item.path : group.index">
        <!-- 独立菜单项 -->
        <el-menu-item v-if="group.type === 'item'" :index="'/' + group.item.path">
          <el-icon v-if="group.item.meta?.icon"><component :is="iconMap[group.item.meta.icon as string]" /></el-icon>
          <template #title>{{ group.item.meta?.title }}</template>
        </el-menu-item>
        <!-- 子菜单组 -->
        <el-sub-menu v-else :index="group.index">
          <template #title>
            <el-icon v-if="group.icon"><component :is="iconMap[group.icon]" /></el-icon>
            <span>{{ group.title }}</span>
          </template>
          <el-menu-item v-for="child in group.children" :key="child.path" :index="'/' + child.path">
            <el-icon v-if="child.meta?.icon"><component :is="iconMap[child.meta.icon as string]" /></el-icon>
            <template #title>{{ child.meta?.title }}</template>
          </el-menu-item>
        </el-sub-menu>
      </template>
    </el-menu>
  </el-aside>
</template>

<style scoped>
.app-sidebar {
  overflow-x: hidden;
  overflow-y: auto;
  background: linear-gradient(180deg, var(--color-surface), var(--color-surface-elevated));
  border-right: 1px solid var(--color-border);
  transition: width 0.3s;
  box-shadow: var(--shadow-sm);
}
.app-sidebar__menu {
  height: 100%;
  border-right: none;
  background: transparent;
}
.app-sidebar__menu:not(.el-menu--collapse) {
  width: 220px;
}

.app-sidebar :deep(.el-menu-item),
.app-sidebar :deep(.el-sub-menu__title) {
  color: var(--color-text-primary);
  font-weight: 600;
}

.app-sidebar :deep(.el-menu-item:hover),
.app-sidebar :deep(.el-sub-menu__title:hover) {
  color: var(--color-primary);
  background: color-mix(in oklab, var(--color-primary) 10%, transparent);
}

.app-sidebar :deep(.el-menu-item.is-active) {
  color: var(--color-primary);
  background: color-mix(in oklab, var(--color-primary) 14%, transparent);
  box-shadow: inset 3px 0 0 var(--color-primary);
}

.app-sidebar.aurora-local {
  background:
    linear-gradient(180deg, rgba(10, 24, 48, 0.96), rgba(8, 16, 34, 0.98)),
    radial-gradient(120% 100% at 20% 0%, rgba(79, 228, 255, 0.16), transparent 65%);
  border-right-color: rgba(82, 204, 255, 0.28);
  box-shadow: 14px 0 28px rgba(2, 14, 36, 0.42);
}

.app-sidebar.aurora-local :deep(.el-menu-item),
.app-sidebar.aurora-local :deep(.el-sub-menu__title) {
  color: #ecf7ff;
  font-weight: 600;
  text-shadow: 0 1px 10px rgba(80, 220, 255, 0.18);
}

.app-sidebar.aurora-local :deep(.el-menu-item .el-icon),
.app-sidebar.aurora-local :deep(.el-sub-menu__title .el-icon),
.app-sidebar.aurora-local :deep(.el-sub-menu__icon-arrow) {
  color: #bceeff;
}

.app-sidebar.aurora-local :deep(.el-menu-item:hover),
.app-sidebar.aurora-local :deep(.el-sub-menu__title:hover) {
  color: #f4fbff;
  background: linear-gradient(90deg, rgba(35, 84, 130, 0.52), rgba(28, 65, 104, 0.34));
}

.app-sidebar.aurora-local :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(22, 68, 112, 0.92), rgba(14, 42, 75, 0.84));
  color: #ffffff;
  box-shadow: inset 3px 0 0 #5ce9ff, 0 0 10px rgba(22, 108, 178, 0.32);
}

.app-sidebar.aurora-local :deep(.el-menu-item.is-active .el-icon) {
  color: #ffffff;
}

.app-sidebar.aurora-local :deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
  color: #ffffff;
  background: linear-gradient(90deg, rgba(18, 58, 98, 0.82), rgba(12, 36, 64, 0.76));
}

.app-sidebar.aurora-local :deep(.el-sub-menu .el-menu-item) {
  background-color: rgba(7, 22, 43, 0.42);
}
</style>
