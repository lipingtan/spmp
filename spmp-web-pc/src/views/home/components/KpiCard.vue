<script setup lang="ts">
defineProps<{
  title: string
  value: string | number
  suffix?: string
}>()
</script>

<template>
  <el-card shadow="never" class="kpi-card">
    <div class="kpi-card__title">{{ title }}</div>
    <div class="kpi-card__value">
      <span>{{ value }}</span>
      <span v-if="suffix" class="kpi-card__suffix">{{ suffix }}</span>
    </div>
  </el-card>
</template>

<style scoped>
.kpi-card {
  min-height: 104px;
}

.kpi-card__title {
  color: var(--color-text-secondary);
  font-size: 13px;
}

.kpi-card__value {
  margin-top: 10px;
  color: var(--color-text-primary);
  font-weight: 700;
  font-size: 28px;
  line-height: 1.1;
}

.kpi-card__suffix {
  margin-left: 4px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
}
</style>

