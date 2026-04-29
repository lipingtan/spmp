<!--
  底部 Tab 栏布局
  - 顶部 van-nav-bar 显示当前路由 meta.title
  - 底部 van-tabbar 5 个 Tab（首页、报修、缴费、公告、我的）
  - safe-area-inset-bottom 适配
  - 内容区 router-view 包裹 transition 过渡动画（slide-left / slide-right）
-->
<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store/modules/app'

const route = useRoute()
const appStore = useAppStore()

/** 当前页面标题 */
const currentTitle = computed(() => (route.meta.title as string) || '智慧物业')

/** 过渡动画名称（由路由守卫设置） */
const transitionName = computed(() => appStore.transitionName)
</script>

<template>
  <div class="tabbar-layout">
    <!-- 顶部导航栏 -->
    <van-nav-bar :title="currentTitle" fixed placeholder />

    <!-- 内容区 -->
    <div class="tabbar-content">
      <router-view v-slot="{ Component }">
        <transition :name="transitionName">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>

    <!-- 底部 Tab 栏 -->
    <van-tabbar route safe-area-inset-bottom>
      <van-tabbar-item icon="wap-home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="orders-o" to="/workorder">报修</van-tabbar-item>
      <van-tabbar-item icon="balance-o" to="/billing">缴费</van-tabbar-item>
      <van-tabbar-item icon="bell" to="/notice">公告</van-tabbar-item>
      <van-tabbar-item icon="contact" to="/mine">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<style scoped>
.tabbar-layout {
  display: flex;
  flex-direction: column;
  height: 100%;
  background:
    radial-gradient(120% 80% at 10% -10%, rgba(61, 139, 255, 0.16), transparent 50%),
    radial-gradient(120% 80% at 100% 0%, rgba(31, 75, 255, 0.12), transparent 52%),
    var(--color-bg-page);
}

.tabbar-content {
  flex: 1;
  overflow-y: auto;
  position: relative;
  padding-bottom: var(--spacing-xs);
}

.tabbar-layout :deep(.van-tabbar-item__icon .van-icon) {
  transition: transform 0.22s ease, text-shadow 0.22s ease, color 0.22s ease;
}

.tabbar-layout :deep(.van-tabbar-item--active .van-tabbar-item__icon .van-icon) {
  animation: tabIconPop 0.28s ease;
  text-shadow: var(--tabbar-active-shadow);
}

/* 左滑进入（目标 Tab 索引 > 当前索引） */
.slide-left-enter-active,
.slide-left-leave-active {
  transition: transform 0.3s ease;
  position: absolute;
  width: 100%;
}
.slide-left-enter-from {
  transform: translateX(100%);
}
.slide-left-leave-to {
  transform: translateX(-100%);
}

/* 右滑返回（目标 Tab 索引 < 当前索引） */
.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.3s ease;
  position: absolute;
  width: 100%;
}
.slide-right-enter-from {
  transform: translateX(-100%);
}
.slide-right-leave-to {
  transform: translateX(100%);
}

@keyframes tabIconPop {
  0% {
    transform: scale(0.85) translateY(1px);
  }
  55% {
    transform: scale(1.16) translateY(-1px);
  }
  100% {
    transform: scale(1);
  }
}
</style>
