<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>&#36894;&#26399;&#20652;&#25910;</template>
      <el-button type="warning" style="margin-bottom:10px" @click="doUrge">&#25209;&#37327;&#20652;&#25910;</el-button>
      <el-table :data="rows" v-loading="loading" @selection-change="selection=$event">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="billNo" label="&#36134;&#21333;&#32534;&#21495;" min-width="180" />
        <el-table-column prop="ownerName" label="&#19994;&#20027;" min-width="120" />
        <el-table-column prop="amount" label="&#24212;&#25910;&#37329;&#39069;" min-width="120" />
        <el-table-column prop="dueDate" label="&#25130;&#27490;&#26085;&#26399;" min-width="140" />
        <el-table-column prop="statusName" label="&#29366;&#24577;" min-width="100" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listOverdueBills, urgeBills } from '@/api/billing/billing'

const loading = ref(false)
const rows = ref<any[]>([])
const selection = ref<any[]>([])

const load = async () => {
  loading.value = true
  try {
    const res: any = await listOverdueBills({ pageNum: 1, pageSize: 50 })
    rows.value = res?.data || []
  } finally { loading.value = false }
}

const doUrge = async () => {
  const ids = selection.value.map((x: any) => x.id)
  if (!ids.length) return
  await urgeBills({ billIds: ids, urgeType: 'MESSAGE', feedback: '\u624b\u52a8\u50ac\u7f34' })
  ElMessage.success('\u50ac\u6536\u6210\u529f')
}

load()
</script>
