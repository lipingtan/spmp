<!--
  顶部导航栏布局（子页面）
  - van-nav-bar 左侧返回箭头 + 标题居中
  - 点击返回调用 router.back()
  - 无底部 Tab 栏
-->
<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

/** 当前页面标题 */
const currentTitle = computed(() => (route.meta.title as string) || '')

/** 返回上一页 */
function onClickLeft() {
  router.back()
}
</script>

<template>
  <div class="navbar-layout">
    <!-- 顶部导航栏 -->
    <van-nav-bar
      :title="currentTitle"
      left-arrow
      fixed
      placeholder
      @click-left="onClickLeft"
    />

    <!-- 内容区 -->
    <div class="navbar-content">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
.navbar-layout {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.navbar-content {
  flex: 1;
  overflow-y: auto;
}
</style>
