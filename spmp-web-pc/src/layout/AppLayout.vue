<!--
  主布局容器
  使用 el-container 组合顶部导航、侧边栏、标签页、面包屑和内容区
-->
<script setup lang="ts">
import { useAppStore } from '@/store/modules/app'
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'
import TagsView from './TagsView.vue'
import AppBreadcrumb from './AppBreadcrumb.vue'

const appStore = useAppStore()
</script>

<template>
  <el-container class="app-layout" :class="{ 'aurora-local': appStore.themeMode === 'aurora-local' }">
    <AppHeader />
    <el-container class="app-layout__body">
      <AppSidebar />
      <el-main class="app-main">
        <TagsView />
        <AppBreadcrumb />
        <div class="app-main__content">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.app-layout {
  height: 100%;
  flex-direction: column;
}

.app-layout__body {
  height: calc(100% - var(--header-height));
  overflow: hidden;
}

.app-main {
  padding: 0;
  overflow-y: auto;
  background-color: var(--color-bg-page);
}

.app-main__content {
  padding: var(--content-padding);
  position: relative;
  z-index: 1;
}

.app-layout.aurora-local .app-main {
  background:
    radial-gradient(130% 100% at 0% 0%, rgba(88, 220, 255, 0.14), transparent 58%),
    linear-gradient(160deg, #071327, #0a1b36 60%, #08172f);
}

.app-layout.aurora-local .app-main__content {
  backdrop-filter: blur(2px);
}
</style>
