<!--
  占位页面
  根据 route.meta.title 显示「{模块名} — 开发中」
-->
<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const moduleName = computed(() => (route.meta.title as string) || '未知模块')
</script>

<template>
  <div class="placeholder-page">
    <el-empty :description="`${moduleName} — 开发中`" />
  </div>
</template>

<style scoped>
.placeholder-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style>
