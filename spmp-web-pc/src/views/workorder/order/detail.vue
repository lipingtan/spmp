<template>
  <div class="page-container" v-loading="loading">
    <el-page-header @back="goBack" :content="`工单详情 - ${detail?.orderNo || ''}`" />

    <div v-if="detail" class="detail-content">
      <el-card shadow="never" class="info-card">
        <template #header><span>基本信息</span></template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="工单编号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="报修类型">{{ orderTypeMap[detail.orderType] || detail.orderType }}</el-descriptions-item>
          <el-descriptions-item label="地址类型">{{ detail.addressType === 'HOUSE' ? '房屋' : '公共区域' }}</el-descriptions-item>
          <el-descriptions-item label="报修人">{{ detail.reporterName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detail.status)" size="small">{{ statusMap[detail.status] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="问题描述" :span="3">{{ detail.description }}</el-descriptions-item>
          <el-descriptions-item label="维修人员">{{ detail.repairUserName || '未派发' }}</el-descriptions-item>
          <el-descriptions-item label="维修时长">{{ detail.repairDuration ? detail.repairDuration + '分钟' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="预计完成">{{ detail.expectedCompleteTime || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="info-card" v-if="detail.reportImages && detail.reportImages.length > 0">
        <template #header><span>报修图片</span></template>
        <el-image
          v-for="(url, idx) in detail.reportImages"
          :key="idx"
          :src="url"
          :preview-src-list="detail.reportImages"
          fit="cover"
          style="width: 120px; height: 120px; margin-right: 8px; border-radius: 4px;"
        />
      </el-card>

      <el-card shadow="never" class="info-card" v-if="detail.dispatchRecords && detail.dispatchRecords.length > 0">
        <template #header><span>派发记录</span></template>
        <el-table :data="detail.dispatchRecords" border size="small">
          <el-table-column prop="repairUserName" label="维修人员" />
          <el-table-column prop="dispatchType" label="派发类型">
            <template #default="{ row }">{{ row.dispatchType === 'MANUAL' ? '手动' : '自动' }}</template>
          </el-table-column>
          <el-table-column prop="dispatcherName" label="派发人" />
          <el-table-column prop="remark" label="备注" />
          <el-table-column prop="dispatchTime" label="派发时间" />
        </el-table>
      </el-card>

      <el-card shadow="never" class="info-card" v-if="detail.materials && detail.materials.length > 0">
        <template #header><span>维修材料</span></template>
        <el-table :data="detail.materials" border size="small">
          <el-table-column prop="materialName" label="材料名称" />
          <el-table-column prop="quantity" label="数量" />
          <el-table-column prop="unit" label="单位" />
          <el-table-column prop="unitPrice" label="单价" />
          <el-table-column prop="totalPrice" label="总价" />
        </el-table>
      </el-card>

      <el-card shadow="never" class="info-card" v-if="detail.evaluation">
        <template #header><span>评价信息</span></template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="评分">
            <el-rate :model-value="detail.evaluation.score" disabled />
          </el-descriptions-item>
          <el-descriptions-item label="评价类型">{{ detail.evaluation.evaluateType === 'OWNER' ? '业主评价' : '自动验收' }}</el-descriptions-item>
          <el-descriptions-item label="评价内容" :span="2">{{ detail.evaluation.content || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="info-card">
        <template #header><span>操作日志</span></template>
        <el-timeline>
          <el-timeline-item
            v-for="log in detail.logs"
            :key="log.id"
            :timestamp="log.operateTime"
            placement="top"
          >
            <div>
              <el-tag size="small" type="info">{{ actionMap[log.action] || log.action }}</el-tag>
              <span style="margin-left: 8px;">{{ log.remark || '' }}</span>
              <span v-if="log.operatorName" style="color: #999; margin-left: 8px;">{{ log.operatorName }}（{{ operatorTypeMap[log.operatorType || ''] || '' }}）</span>
            </div>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getWorkOrderDetail } from '@/api/workorder/workOrder'
import type { WorkOrderDetailVO } from '@/api/workorder/workOrder'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<WorkOrderDetailVO | null>(null)

const statusMap: Record<string, string> = {
  PENDING_DISPATCH: '待派发',
  PENDING_ACCEPT: '待接单',
  IN_PROGRESS: '处理中',
  PENDING_VERIFY: '待验收',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  FORCE_CLOSED: '已关闭'
}

const orderTypeMap: Record<string, string> = {
  WATER_ELECTRIC: '水电维修',
  DOOR_WINDOW: '门窗维修',
  PIPELINE: '管道疏通',
  PUBLIC_FACILITY: '公共设施',
  OTHER: '其他'
}

const actionMap: Record<string, string> = {
  CREATE: '创建',
  DISPATCH: '派发',
  ACCEPT: '接单',
  COMPLETE: '完成',
  VERIFY_PASS: '验收通过',
  VERIFY_REJECT: '验收不通过',
  TRANSFER: '转派',
  CANCEL: '取消',
  FORCE_CLOSE: '强制关闭',
  AUTO_DISPATCH: '自动派发',
  TIMEOUT_RETURN: '超时退回',
  AUTO_VERIFY: '自动验收',
  URGE: '催单'
}

const operatorTypeMap: Record<string, string> = {
  OWNER: '业主',
  ADMIN: '管理员',
  REPAIR: '维修人员',
  SYSTEM: '系统'
}

function statusTagType(status: string): string {
  const map: Record<string, string> = {
    PENDING_DISPATCH: 'warning',
    PENDING_ACCEPT: 'warning',
    IN_PROGRESS: 'primary',
    PENDING_VERIFY: 'info',
    COMPLETED: 'success',
    CANCELLED: 'info',
    FORCE_CLOSED: 'danger'
  }
  return map[status] || ''
}

function goBack() {
  router.push('/workorder/list')
}

async function fetchDetail() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  try {
    detail.value = await getWorkOrderDetail(id)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.page-container { padding: 16px; }
.detail-content { margin-top: 16px; }
.info-card { margin-bottom: 16px; }
</style>
