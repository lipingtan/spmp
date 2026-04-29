<!--
  404 页面
  van-empty 显示「页面不存在」+ van-button「返回首页」
-->
<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/home')
}
</script>

<template>
  <div class="not-found-page">
    <van-empty description="页面不存在">
      <van-button type="primary" round @click="goHome">返回首页</van-button>
    </van-empty>
  </div>
</template>

<style scoped>
.not-found-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
</style>
