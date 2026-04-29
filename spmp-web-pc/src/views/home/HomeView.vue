<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import KpiCard from './components/KpiCard.vue'
import QuickEntry from './components/QuickEntry.vue'
import {
  getDashboardBillingTrend,
  getDashboardKpi,
  getDashboardWorkorderTrend,
  type BillingTrendPoint,
  type DashboardKpi,
  type ModuleStatus,
  type TimeRange,
  type WorkorderTrendPoint
} from '@/api/home/dashboard'

const userStore = useUserStore()
const router = useRouter()

const timeRange = ref<TimeRange>('MONTH')
const loading = ref(false)
const kpiStatus = ref<ModuleStatus>('SUCCESS')
const kpiMessage = ref('')
const kpis = ref<DashboardKpi>({
  pendingCount: 0,
  inProgressCount: 0,
  monthlyCompletedCount: 0,
  avgRepairDuration: 0,
  totalReceivable: 0,
  totalReceived: 0,
  collectionRate: 0,
  overdueAmount: 0,
  recentNoticeCount: 0
})
const workorderStatus = ref<ModuleStatus>('SUCCESS')
const workorderMessage = ref('')
const workorderTrend = ref<WorkorderTrendPoint[]>([])
const billingStatus = ref<ModuleStatus>('SUCCESS')
const billingMessage = ref('')
const billingTrend = ref<BillingTrendPoint[]>([])

const workorderChartRef = ref<HTMLDivElement>()
const billingChartRef = ref<HTMLDivElement>()
let workorderChart: echarts.ECharts | null = null
let billingChart: echarts.ECharts | null = null

const quickEntries = reactive([
  { title: '工单列表', permission: 'workorder:list', route: '/workorder/list' },
  { title: '工单统计', permission: 'workorder:statistics', route: '/workorder/statistics' },
  { title: '账单管理', permission: 'billing:bill:list', route: '/billing/bills' },
  { title: '收费统计', permission: 'billing:statistics', route: '/billing/statistics' },
  { title: '公告列表', permission: 'notice:list', route: '/notice/list' },
  { title: '认证审批', permission: 'owner:certification:list', route: '/owner/certifications' }
])

const visibleQuickEntries = computed(() => quickEntries.filter(item => userStore.hasPermission(item.permission)))

function formatCurrency(value: number) {
  return Number(value || 0).toFixed(2)
}

async function fetchDashboard() {
  loading.value = true
  const [kpiRes, workorderRes, billingRes] = await Promise.allSettled([
    getDashboardKpi(timeRange.value),
    getDashboardWorkorderTrend(timeRange.value),
    getDashboardBillingTrend(timeRange.value)
  ])
  if (kpiRes.status === 'fulfilled') {
    kpiStatus.value = kpiRes.value.status
    kpiMessage.value = kpiRes.value.message || ''
    kpis.value = kpiRes.value.data || kpis.value
  } else {
    kpiStatus.value = 'ERROR'
    kpiMessage.value = 'KPI 数据加载失败'
  }
  if (workorderRes.status === 'fulfilled') {
    workorderStatus.value = workorderRes.value.status
    workorderMessage.value = workorderRes.value.message || ''
    workorderTrend.value = workorderRes.value.data || []
  } else {
    workorderStatus.value = 'ERROR'
    workorderMessage.value = '工单趋势加载失败'
    workorderTrend.value = []
  }
  if (billingRes.status === 'fulfilled') {
    billingStatus.value = billingRes.value.status
    billingMessage.value = billingRes.value.message || ''
    billingTrend.value = billingRes.value.data || []
  } else {
    billingStatus.value = 'ERROR'
    billingMessage.value = '收费趋势加载失败'
    billingTrend.value = []
  }
  loading.value = false
  await nextTick()
  renderCharts()
}

function renderCharts() {
  if (workorderChartRef.value) {
    workorderChart = workorderChart || echarts.init(workorderChartRef.value)
    workorderChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: workorderTrend.value.map(i => i.date) },
      yAxis: { type: 'value' },
      series: [{ type: 'line', smooth: true, data: workorderTrend.value.map(i => i.value) }]
    })
  }
  if (billingChartRef.value) {
    billingChart = billingChart || echarts.init(billingChartRef.value)
    billingChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['应收', '实收', '收缴率'] },
      xAxis: { type: 'category', data: billingTrend.value.map(i => i.period) },
      yAxis: [{ type: 'value' }, { type: 'value', max: 100 }],
      series: [
        { name: '应收', type: 'line', data: billingTrend.value.map(i => i.receivable) },
        { name: '实收', type: 'line', data: billingTrend.value.map(i => i.received) },
        { name: '收缴率', type: 'line', yAxisIndex: 1, data: billingTrend.value.map(i => i.collectionRate) }
      ]
    })
  }
}

function goTo(route: string) {
  router.push(route)
}

onMounted(fetchDashboard)
</script>

<template>
  <div class="home-page">
    <el-card class="home-welcome" shadow="never">
      <div class="home-header">
        <div>
          <div class="home-welcome__text">你好，{{ userStore.username || '用户' }}！</div>
          <div class="home-welcome__hint">欢迎使用智慧物业管理平台 Dashboard</div>
        </div>
        <el-select v-model="timeRange" style="width: 140px" @change="fetchDashboard">
          <el-option label="本月" value="MONTH" />
          <el-option label="本季度" value="QUARTER" />
          <el-option label="本年" value="YEAR" />
        </el-select>
      </div>
    </el-card>

    <el-alert
      v-if="kpiStatus !== 'SUCCESS'"
      :title="kpiMessage || '暂无KPI数据'"
      type="warning"
      :closable="false"
      show-icon
    />
    <el-row v-else v-loading="loading" :gutter="12" class="kpi-row">
      <el-col :span="6"><KpiCard title="待处理工单" :value="kpis.pendingCount" /></el-col>
      <el-col :span="6"><KpiCard title="处理中工单" :value="kpis.inProgressCount" /></el-col>
      <el-col :span="6"><KpiCard title="本月完成" :value="kpis.monthlyCompletedCount" /></el-col>
      <el-col :span="6"><KpiCard title="平均维修时长" :value="kpis.avgRepairDuration" suffix="分钟" /></el-col>
      <el-col :span="6"><KpiCard title="应收金额" :value="formatCurrency(kpis.totalReceivable)" suffix="元" /></el-col>
      <el-col :span="6"><KpiCard title="实收金额" :value="formatCurrency(kpis.totalReceived)" suffix="元" /></el-col>
      <el-col :span="6"><KpiCard title="收缴率" :value="formatCurrency(kpis.collectionRate)" suffix="%" /></el-col>
      <el-col :span="6"><KpiCard title="最近7天公告数" :value="kpis.recentNoticeCount" /></el-col>
    </el-row>

    <el-card shadow="never" class="chart-card">
      <template #header><span>工单趋势</span></template>
      <el-empty v-if="workorderStatus !== 'SUCCESS'" :description="workorderMessage || '暂无数据'" />
      <div v-else ref="workorderChartRef" class="chart" />
    </el-card>

    <el-card shadow="never" class="chart-card">
      <template #header><span>收费趋势</span></template>
      <el-empty v-if="billingStatus !== 'SUCCESS'" :description="billingMessage || '暂无数据'" />
      <div v-else ref="billingChartRef" class="chart" />
    </el-card>

    <el-card shadow="never">
      <template #header><span>快捷入口</span></template>
      <QuickEntry
        v-for="item in visibleQuickEntries"
        :key="item.route"
        :title="item.title"
        :permission="item.permission"
        :route="item.route"
        :visible="true"
        @go="goTo"
      />
      <el-empty v-if="visibleQuickEntries.length === 0" description="暂无可用快捷入口" />
    </el-card>
  </div>
</template>

<style scoped>
.home-page {
  display: grid;
  gap: 12px;
}

.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.home-welcome__text {
  font-size: 16px;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-sm);
}

.home-welcome__hint {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.kpi-row :deep(.el-col) {
  margin-bottom: 12px;
}

.chart-card {
  min-height: 360px;
}

.chart {
  height: 300px;
}
</style>
