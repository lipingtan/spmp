<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="时间范围">
          <el-select v-model="queryParams.timeRange" placeholder="请选择" style="min-width: 140px" @change="handleSearch">
            <el-option label="今天" value="TODAY" />
            <el-option label="近一周" value="WEEK" />
            <el-option label="近一月" value="MONTH" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" class="stat-cards">
      <el-col :span="5">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ stats.pendingCount }}</div>
          <div class="stat-label">待处理</div>
        </el-card>
      </el-col>
      <el-col :span="5">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ stats.inProgressCount }}</div>
          <div class="stat-label">处理中</div>
        </el-card>
      </el-col>
      <el-col :span="5">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ stats.monthlyCompletedCount }}</div>
          <div class="stat-label">本月完成</div>
        </el-card>
      </el-col>
      <el-col :span="5">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ stats.avgRepairDuration }}<span class="stat-unit">分钟</span></div>
          <div class="stat-label">平均维修时长</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ stats.avgScore }}<span class="stat-unit">星</span></div>
          <div class="stat-label">平均满意度</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>每日工单量趋势</span></template>
          <div ref="dailyChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>月度满意度趋势</span></template>
          <div ref="scoreChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { getStatistics } from '@/api/workorder/workOrder'
import type { StatisticsVO, TrendDataVO } from '@/api/workorder/workOrder'
import * as echarts from 'echarts'

const dailyChartRef = ref<HTMLElement>()
const scoreChartRef = ref<HTMLElement>()
let dailyChart: echarts.ECharts | null = null
let scoreChart: echarts.ECharts | null = null

const stats = ref<StatisticsVO>({
  pendingCount: 0,
  inProgressCount: 0,
  monthlyCompletedCount: 0,
  avgRepairDuration: 0,
  avgScore: 0,
  dailyTrend: [],
  monthlyScoreTrend: []
})

const queryParams = reactive({
  timeRange: 'MONTH'
})

async function fetchData() {
  const res: any = await getStatistics(queryParams)
  stats.value = res || stats.value
  await nextTick()
  renderCharts()
}

function renderCharts() {
  renderDailyChart(stats.value.dailyTrend || [])
  renderScoreChart(stats.value.monthlyScoreTrend || [])
}

function renderDailyChart(data: TrendDataVO[]) {
  if (!dailyChartRef.value) return
  if (!dailyChart) {
    dailyChart = echarts.init(dailyChartRef.value)
  }
  dailyChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map(d => d.date) },
    yAxis: { type: 'value' },
    series: [{ data: data.map(d => d.value), type: 'line', smooth: true, areaStyle: { opacity: 0.3 } }]
  })
}

function renderScoreChart(data: TrendDataVO[]) {
  if (!scoreChartRef.value) return
  if (!scoreChart) {
    scoreChart = echarts.init(scoreChartRef.value)
  }
  scoreChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map(d => d.date) },
    yAxis: { type: 'value', min: 0, max: 5 },
    series: [{ data: data.map(d => d.value), type: 'bar' }]
  })
}

function handleSearch() {
  fetchData()
}

function handleResize() {
  dailyChart?.resize()
  scoreChart?.resize()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  dailyChart?.dispose()
  scoreChart?.dispose()
})
</script>

<style scoped>
.page-container { padding: 16px; }
.search-card { margin-bottom: 16px; }
.stat-cards { margin-bottom: 16px; }
.stat-card { text-align: center; padding: 8px 0; }
.stat-value { font-size: 28px; font-weight: 600; color: #409eff; }
.stat-unit { font-size: 14px; color: #999; margin-left: 4px; }
.stat-label { font-size: 13px; color: #999; margin-top: 4px; }
.chart-container { height: 320px; }
</style>
