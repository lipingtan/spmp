<!--
  H5 维修人员端 — 工作台
  - 今日待处理数 + 本月完成数
  - 快捷入口
-->
<template>
  <div class="repair-dashboard">
    <van-nav-bar title="维修工作台" />

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-value">{{ displayTodayPending }}</div>
        <div class="stat-label">今日待处理</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ displayMonthlyCompleted }}</div>
        <div class="stat-label">本月完成</div>
      </div>
    </div>

    <van-cell-group inset title="快捷入口" class="entry-group">
      <van-cell title="待处理工单" is-link icon="orders-o" to="/repair/pending" />
      <van-cell title="历史工单" is-link icon="clock-o" to="/repair/history" />
      <van-cell title="切换到业主端" is-link icon="exchange" to="/home" />
    </van-cell-group>

    <div class="logout-wrap">
      <van-button block round plain type="danger" @click="userStore.logout()">退出登录</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDashboard } from '@/api/repair'
import type { RepairDashboardVO } from '@/api/repair'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

const dashboard = ref<RepairDashboardVO>({
  todayPendingCount: 0,
  monthlyCompletedCount: 0
})
const displayTodayPending = ref(0)
const displayMonthlyCompleted = ref(0)

function animateNumber(targetRef: { value: number }, target: number, duration = 560) {
  const start = targetRef.value
  const delta = target - start
  const startAt = performance.now()
  const step = (now: number) => {
    const progress = Math.min((now - startAt) / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    targetRef.value = Math.round(start + delta * eased)
    if (progress < 1) {
      requestAnimationFrame(step)
    }
  }
  requestAnimationFrame(step)
}

async function fetchDashboard() {
  try {
    const res: any = await getDashboard()
    dashboard.value = res || dashboard.value
    animateNumber(displayTodayPending, dashboard.value.todayPendingCount || 0)
    animateNumber(displayMonthlyCompleted, dashboard.value.monthlyCompletedCount || 0)
  } catch {
    // ignore
  }
}

onMounted(() => {
  fetchDashboard()
})
</script>

<style scoped>
.repair-dashboard {
  min-height: 100vh;
  background:
    radial-gradient(100% 80% at 0% 0%, rgba(125, 166, 255, 0.12), transparent 55%),
    radial-gradient(120% 100% at 100% 0%, rgba(212, 175, 55, 0.1), transparent 60%),
    var(--color-bg-page);
}
.stats-row {
  display: flex;
  gap: 12px;
  padding: 16px;
}
.stat-card {
  flex: 1;
  background: linear-gradient(165deg, var(--color-surface), var(--color-surface-elevated));
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--glass-border);
  padding: 20px;
  text-align: center;
  box-shadow: var(--shadow-md);
}
.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: var(--color-primary);
  text-shadow: var(--tabbar-active-shadow);
}
.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}
.entry-group {
  margin-top: 8px;
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.logout-wrap {
  margin-top: 16px;
  padding: 0 16px 16px;
}
</style>
