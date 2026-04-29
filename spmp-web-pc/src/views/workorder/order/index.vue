<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="工单状态">
          <el-select v-model="queryParams.status" style="min-width: 140px">
            <el-option label="全部" value="" />
            <el-option label="待派发" value="PENDING_DISPATCH" />
            <el-option label="待接单" value="PENDING_ACCEPT" />
            <el-option label="处理中" value="IN_PROGRESS" />
            <el-option label="待验收" value="PENDING_VERIFY" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="已关闭" value="FORCE_CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="报修类型">
          <el-select v-model="queryParams.orderType" style="min-width: 140px">
            <el-option label="全部" value="" />
            <el-option label="水电维修" value="WATER_ELECTRIC" />
            <el-option label="门窗维修" value="DOOR_WINDOW" />
            <el-option label="管道疏通" value="PIPELINE" />
            <el-option label="公共设施" value="PUBLIC_FACILITY" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="queryParams.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="queryParams.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="工单编号/报修人" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <span>工单列表</span>
      </template>

      <el-table v-loading="loading" :data="tableData" row-key="id">
        <el-table-column prop="orderNo" label="工单编号" width="160" />
        <el-table-column prop="orderType" label="报修类型" width="110">
          <template #default="{ row }">
            {{ orderTypeMap[row.orderType] || row.orderType }}
          </template>
        </el-table-column>
        <el-table-column prop="reporterName" label="报修人" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="urgeCount" label="催单" width="70" align="center" />
        <el-table-column prop="rejectCount" label="驳回" width="70" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column prop="repairDuration" label="维修时长" width="100">
          <template #default="{ row }">
            {{ row.repairDuration ? row.repairDuration + '分钟' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="primary" v-if="row.status === 'PENDING_DISPATCH'" v-permission="'workorder:dispatch'" @click="handleDispatch(row)">派发</el-button>
            <el-button link type="danger" v-if="canCancel(row.status)" v-permission="'workorder:cancel'" @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <el-dialog v-model="dispatchVisible" title="派发工单" width="500px" destroy-on-close>
      <el-form :model="dispatchForm" label-width="100px">
        <el-form-item label="维修人员" required>
          <el-select v-model="dispatchForm.repairUserId" placeholder="请选择维修人员" filterable style="width: 100%">
            <el-option v-for="s in staffList" :key="s.userId" :label="`${s.realName}（${s.phone}）- 当前${s.currentWorkload}单`" :value="s.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dispatchForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="预计完成">
          <el-date-picker v-model="dispatchForm.expectedCompleteTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择预计完成时间" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dispatchVisible = false">取消</el-button>
        <el-button type="primary" :loading="dispatching" @click="submitDispatch">确认派发</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="cancelVisible" title="取消/关闭工单" width="500px" destroy-on-close>
      <el-form :model="cancelForm" label-width="100px">
        <el-form-item label="操作类型">
          <el-radio-group v-model="cancelForm.cancelType">
            <el-radio label="CANCEL">取消</el-radio>
            <el-radio label="FORCE_CLOSE">强制关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="原因" required>
          <el-input v-model="cancelForm.cancelReason" type="textarea" :rows="3" placeholder="请输入原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelVisible = false">取消</el-button>
        <el-button type="danger" :loading="cancelling" @click="submitCancel">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listWorkOrders, dispatchWorkOrder, cancelWorkOrder, listRepairStaff } from '@/api/workorder/workOrder'
import type { WorkOrderListVO, RepairStaffVO } from '@/api/workorder/workOrder'

const router = useRouter()
const loading = ref(false)
const tableData = ref<WorkOrderListVO[]>([])
const total = ref(0)

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

const queryParams = reactive({
  status: '' as string,
  orderType: '' as string,
  communityId: undefined as number | undefined,
  buildingId: undefined as number | undefined,
  startDate: '' as string,
  endDate: '' as string,
  keyword: '' as string,
  pageNum: 1,
  pageSize: 10
})

const dispatchVisible = ref(false)
const dispatching = ref(false)
const staffList = ref<RepairStaffVO[]>([])
const currentOrderId = ref<number>(0)
const dispatchForm = reactive({
  repairUserId: undefined as number | undefined,
  remark: '',
  expectedCompleteTime: ''
})

const cancelVisible = ref(false)
const cancelling = ref(false)
const cancelForm = reactive({
  cancelType: 'CANCEL',
  cancelReason: ''
})

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

function canCancel(status: string): boolean {
  return ['PENDING_DISPATCH', 'PENDING_ACCEPT', 'IN_PROGRESS'].includes(status)
}

async function fetchData() {
  loading.value = true
  try {
    // 过滤空字符串参数，避免后端 LocalDate 类型转换失败
    const params: Record<string, any> = {}
    Object.entries(queryParams).forEach(([k, v]) => {
      if (v !== '' && v !== undefined && v !== null) {
        params[k] = v
      }
    })
    const res: any = await listWorkOrders(params as any)
    tableData.value = res.data || res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.pageNum = 1
  fetchData()
}

function handleReset() {
  queryParams.status = ''
  queryParams.orderType = ''
  queryParams.communityId = undefined
  queryParams.buildingId = undefined
  queryParams.startDate = ''
  queryParams.endDate = ''
  queryParams.keyword = ''
  handleSearch()
}

function handleDetail(row: WorkOrderListVO) {
  router.push(`/workorder/list/${row.id}`)
}

async function handleDispatch(row: WorkOrderListVO) {
  currentOrderId.value = row.id
  dispatchForm.repairUserId = undefined
  dispatchForm.remark = ''
  dispatchForm.expectedCompleteTime = ''
  const staff: any = await listRepairStaff()
  staffList.value = staff || []
  dispatchVisible.value = true
}

async function submitDispatch() {
  if (!dispatchForm.repairUserId) {
    ElMessage.warning('请选择维修人员')
    return
  }
  dispatching.value = true
  try {
    await dispatchWorkOrder(currentOrderId.value, {
      repairUserId: dispatchForm.repairUserId,
      remark: dispatchForm.remark,
      expectedCompleteTime: dispatchForm.expectedCompleteTime || undefined
    })
    ElMessage.success('派发成功')
    dispatchVisible.value = false
    fetchData()
  } finally {
    dispatching.value = false
  }
}

function handleCancel(row: WorkOrderListVO) {
  currentOrderId.value = row.id
  cancelForm.cancelType = 'CANCEL'
  cancelForm.cancelReason = ''
  cancelVisible.value = true
}

async function submitCancel() {
  if (!cancelForm.cancelReason) {
    ElMessage.warning('请输入原因')
    return
  }
  cancelling.value = true
  try {
    await cancelWorkOrder(currentOrderId.value, {
      cancelReason: cancelForm.cancelReason,
      cancelType: cancelForm.cancelType
    })
    ElMessage.success('操作成功')
    cancelVisible.value = false
    fetchData()
  } finally {
    cancelling.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 16px; }
.search-card { margin-bottom: 16px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
