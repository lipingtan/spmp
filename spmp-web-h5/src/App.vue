<script setup lang="ts">
// 根组件，包含 router-view 和过渡动画容器
import { useAppStore } from '@/store/modules/app'

const appStore = useAppStore()
</script>

<template>
  <div class="app-shell">
    <div class="app-bg">
      <span class="orb orb-a"></span>
      <span class="orb orb-b"></span>
      <span class="orb orb-c"></span>
    </div>
    <div class="theme-switch-overlay" :class="{ active: appStore.themeTransitionActive }"></div>
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </div>
</template>

<style>
/* 全局样式已通过 styles/global.css 引入 */
#app {
  height: 100%;
}

.app-shell {
  position: relative;
  min-height: 100%;
}

.app-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(2px);
  opacity: 0.7;
}

.orb-a {
  width: 220px;
  height: 220px;
  left: -60px;
  top: 12%;
  background: radial-gradient(circle at 30% 30%, rgba(126, 175, 255, 0.52), transparent 70%);
  animation: floatA 14s ease-in-out infinite;
}

.orb-b {
  width: 280px;
  height: 280px;
  right: -90px;
  top: 28%;
  background: radial-gradient(circle at 40% 40%, rgba(212, 175, 55, 0.34), transparent 72%);
  animation: floatB 18s ease-in-out infinite;
}

.orb-c {
  width: 180px;
  height: 180px;
  left: 36%;
  bottom: -40px;
  background: radial-gradient(circle at 40% 40%, rgba(116, 137, 255, 0.28), transparent 72%);
  animation: floatC 16s ease-in-out infinite;
}

.theme-switch-overlay {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 999;
  opacity: 0;
  background:
    radial-gradient(120% 90% at 20% 20%, rgba(125, 166, 255, 0.24), transparent 55%),
    radial-gradient(120% 90% at 80% 25%, rgba(212, 175, 55, 0.22), transparent 58%),
    rgba(10, 14, 24, 0.18);
  transition: opacity 0.42s ease;
}

.theme-switch-overlay.active {
  opacity: 1;
}

/* 淡入淡出过渡动画（用于顶层路由切换，如登录→主布局） */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.32s ease, transform 0.32s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.99);
}

@keyframes floatA {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50% { transform: translate3d(20px, -18px, 0) scale(1.08); }
}

@keyframes floatB {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50% { transform: translate3d(-24px, 12px, 0) scale(1.06); }
}

@keyframes floatC {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50% { transform: translate3d(10px, -16px, 0) scale(1.12); }
}
</style>
