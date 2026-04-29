<!--
  面包屑导航
  - 根据 route.matched 自动生成
  - 首项固定「首页」，链接到 /home
  - 最后一项不可点击
-->
<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

/** 面包屑列表 */
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(
    (item) => item.meta?.title && !item.meta?.hidden
  )

  const items: Array<{ title: string; path: string }> = []

  // 首项固定「首页」
  if (route.path !== '/home') {
    items.push({ title: '首页', path: '/home' })
  }

  matched.forEach((item) => {
    if (item.meta?.title) {
      items.push({
        title: item.meta.title as string,
        path: item.path
      })
    }
  })

  return items
})
</script>

<template>
  <el-breadcrumb separator="/" class="app-breadcrumb">
    <el-breadcrumb-item
      v-for="(item, index) in breadcrumbs"
      :key="item.path"
      :to="index < breadcrumbs.length - 1 ? { path: item.path } : undefined"
    >
      {{ item.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<style scoped>
.app-breadcrumb {
  padding: var(--spacing-lg) var(--spacing-xl);
}
</style>
