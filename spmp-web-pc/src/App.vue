<script setup lang="ts">
// 根组件 — 路由出口
import { useAppStore } from '@/store/modules/app'

const appStore = useAppStore()
</script>

<template>
  <div class="pc-shell">
    <div class="pc-bg">
      <span class="pc-orb pc-orb-a"></span>
      <span class="pc-orb pc-orb-b"></span>
      <span class="pc-orb pc-orb-c"></span>
    </div>
    <div class="theme-switch-overlay" :class="{ active: appStore.themeTransitionActive }"></div>
    <router-view />
  </div>
</template>

<style>
#app {
  height: 100%;
}

.pc-shell {
  position: relative;
  min-height: 100%;
}

.pc-bg {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.pc-orb {
  position: absolute;
  border-radius: 50%;
}

.pc-orb-a {
  width: 420px;
  height: 420px;
  left: -120px;
  top: -80px;
  background: radial-gradient(circle at 30% 30%, rgba(125, 166, 255, 0.24), transparent 70%);
  animation: pcFloatA 18s ease-in-out infinite;
}

.pc-orb-b {
  width: 380px;
  height: 380px;
  right: -120px;
  top: 15%;
  background: radial-gradient(circle at 40% 35%, rgba(212, 175, 55, 0.2), transparent 72%);
  animation: pcFloatB 20s ease-in-out infinite;
}

.pc-orb-c {
  width: 300px;
  height: 300px;
  left: 35%;
  bottom: -120px;
  background: radial-gradient(circle at 40% 40%, rgba(94, 123, 255, 0.16), transparent 74%);
  animation: pcFloatC 16s ease-in-out infinite;
}

.theme-switch-overlay {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 999;
  opacity: 0;
  background:
    radial-gradient(120% 90% at 20% 20%, rgba(125, 166, 255, 0.2), transparent 56%),
    radial-gradient(120% 90% at 80% 25%, rgba(212, 175, 55, 0.2), transparent 58%),
    rgba(6, 10, 18, 0.16);
  transition: opacity 0.42s ease;
}

.theme-switch-overlay.active {
  opacity: 1;
}

@keyframes pcFloatA {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50% { transform: translate3d(20px, 22px, 0) scale(1.06); }
}
@keyframes pcFloatB {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50% { transform: translate3d(-20px, -16px, 0) scale(1.08); }
}
@keyframes pcFloatC {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50% { transform: translate3d(14px, -20px, 0) scale(1.1); }
}
</style>
