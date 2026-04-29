<template>
  <div class="page">
    <el-row :gutter="12" class="kpi-row">
      <el-col :span="6"><el-card>&#24212;&#25910;&#24635;&#39069;&#65306;{{ stats.totalReceivable ?? 0 }}</el-card></el-col>
      <el-col :span="6"><el-card>&#23454;&#25910;&#24635;&#39069;&#65306;{{ stats.totalReceived ?? 0 }}</el-card></el-col>
      <el-col :span="6"><el-card>&#25910;&#32564;&#29575;&#65306;{{ stats.collectionRate ?? 0 }}%</el-card></el-col>
      <el-col :span="6"><el-card>&#36894;&#26399;&#37329;&#39069;&#65306;{{ stats.overdueAmount ?? 0 }}</el-card></el-col>
    </el-row>
    <el-card shadow="never">
      <template #header>&#25910;&#36153;&#36235;&#21183;</template>
      <el-table :data="stats.trends || []" v-loading="loading">
        <el-table-column prop="period" label="&#21608;&#26399;" min-width="120" />
        <el-table-column prop="receivable" label="&#24212;&#25910;" min-width="120" />
        <el-table-column prop="received" label="&#23454;&#25910;" min-width="120" />
        <el-table-column prop="collectionRate" label="&#25910;&#32564;&#29575;(%)" min-width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getBillStatistics, type StatisticsVO } from '@/api/billing/billing'

const loading = ref(false)
const stats = ref<Partial<StatisticsVO>>({})

onMounted(async () => {
  loading.value = true
  try {
    stats.value = await getBillStatistics({})
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.kpi-row { margin-bottom: 12px; }
</style>
