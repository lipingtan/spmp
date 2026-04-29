<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="认证状态">
          <el-select v-model="queryParams.certStatus" placeholder="全部" clearable style="width: 160px">
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="小区">
          <el-select v-model="queryParams.communityId" placeholder="全部" clearable filterable style="width: 220px" @change="onCommunityChange">
            <el-option v-for="c in communityOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼栋">
          <el-select v-model="queryParams.buildingId" placeholder="全部" clearable filterable style="width: 200px" :disabled="!queryParams.communityId">
            <el-option v-for="b in buildingOptions" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 + 表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>认证审批列表</span>
          <div v-if="hasPermission('owner:certify:approve')">
            <el-button type="primary" :disabled="!selectedIds.length" @click="handleBatchApprove">批量通过</el-button>
            <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchReject">批量驳回</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" row-key="id" @selection-change="onSelectionChange">
        <el-table-column type="selection" width="50" :selectable="canSelect" />
        <el-table-column prop="ownerName" label="业主姓名" min-width="100" />
        <el-table-column prop="communityName" label="小区" min-width="120" show-overflow-tooltip />
        <el-table-column prop="buildingName" label="楼栋" width="100" />
        <el-table-column prop="unitName" label="单元" width="80" />
        <el-table-column prop="houseName" label="房屋" width="100" />
        <el-table-column prop="certStatus" label="认证状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.certStatus)" size="small">{{ statusText(row.certStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="驳回原因" min-width="150" show-overflow-tooltip />
        <el-table-column prop="applyTime" label="申请时间" width="180" />
        <el-table-column prop="approveTime" label="审批时间" width="180" />
        <el-table-column prop="approverName" label="审批人" width="100" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.certStatus === 'PENDING' && hasPermission('owner:certify:approve')">
              <el-button link type="primary" @click="handleApprove(row)">通过</el-button>
              <el-button link type="danger" @click="handleReject(row)">驳回</el-button>
            </template>
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
  </div>
</template>

<script setup lang="ts">
/**
 * 认证审批页面
 * 功能：搜索、分页列表、单条审批（通过/驳回）、批量审批
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCertifications, approveCertification, batchApproveCertifications } from '@/api/owner/certification'
import type { CertificationVO } from '@/api/owner/certification'
import type { CascadeItem } from '@/api/base/cascade'
import { getCascadeData } from '@/api/base/cascade'
import { useUserStore } from '@/store/modules/user'
import { useBaseStore } from '@/store/modules/base'

const userStore = useUserStore()
const baseStore = useBaseStore()

const loading = ref(false)
const tableData = ref<CertificationVO[]>([])
const total = ref(0)

/** 批量选中的 ID 列表 */
const selectedIds = ref<number[]>([])

/** 小区下拉选项 */
const communityOptions = ref<CascadeItem[]>([])
/** 楼栋下拉选项（依赖小区） */
const buildingOptions = ref<CascadeItem[]>([])

/** 查询参数 */
const queryParams = reactive({
  certStatus: undefined as string | undefined,
  communityId: undefined as number | undefined,
  buildingId: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10
})

/** 权限检查 */
function hasPermission(perm: string): boolean {
  return userStore.hasPermission(perm)
}

/** 认证状态标签类型 */
function statusTagType(status: string): '' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<string, '' | 'success' | 'warning' | 'danger' | 'info'> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return map[status] ?? 'info'
}

/** 认证状态文本 */
function statusText(status: string): string {
  const map: Record<string, string> = {
    PENDING: '待审批',
    APPROVED: '已通过',
    REJECTED: '已驳回'
  }
  return map[status] ?? status
}

/** 仅待审批的行可勾选 */
function canSelect(row: CertificationVO): boolean {
  return row.certStatus === 'PENDING'
}

/** 加载列表数据 */
async function fetchData() {
  loading.value = true
  try {
    const res: any = await listCertifications(queryParams)
    tableData.value = res.data || res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

/** 查询 */
function handleSearch() {
  queryParams.pageNum = 1
  fetchData()
}

/** 重置 */
function handleReset() {
  queryParams.certStatus = undefined
  queryParams.communityId = undefined
  queryParams.buildingId = undefined
  buildingOptions.value = []
  handleSearch()
}

/** 小区变更时联动加载楼栋 */
async function onCommunityChange(val: number | undefined) {
  queryParams.buildingId = undefined
  buildingOptions.value = []
  if (val) {
    buildingOptions.value = await baseStore.loadBuildings(val)
  }
}

/** 表格多选变更 */
function onSelectionChange(rows: CertificationVO[]) {
  selectedIds.value = rows.map(r => r.id)
}

/** 单条通过 */
async function handleApprove(row: CertificationVO) {
  await ElMessageBox.confirm(`确认通过业主「${row.ownerName}」的认证申请？`, '提示', { type: 'info' })
  await approveCertification(row.id, { action: 'APPROVE' })
  ElMessage.success('审批通过')
  fetchData()
}

/** 单条驳回 */
async function handleReject(row: CertificationVO) {
  const { value } = await ElMessageBox.prompt(
    `请输入驳回业主「${row.ownerName}」的原因`,
    '驳回原因',
    {
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
      type: 'warning',
      inputPlaceholder: '请输入驳回原因',
      inputType: 'textarea',
      inputValidator: (val: string) => {
        if (!val || !val.trim()) return '驳回原因不能为空'
        return true
      }
    }
  )
  await approveCertification(row.id, { action: 'REJECT', rejectReason: value })
  ElMessage.success('已驳回')
  fetchData()
}

/** 批量通过 */
async function handleBatchApprove() {
  if (selectedIds.value.length > 100) {
    ElMessage.warning('单次批量审批最多 100 条')
    return
  }
  await ElMessageBox.confirm(`确认批量通过选中的 ${selectedIds.value.length} 条认证申请？`, '提示', { type: 'info' })
  await batchApproveCertifications({ ids: selectedIds.value, action: 'APPROVE' })
  ElMessage.success('批量审批通过')
  fetchData()
}

/** 批量驳回 */
async function handleBatchReject() {
  if (selectedIds.value.length > 100) {
    ElMessage.warning('单次批量审批最多 100 条')
    return
  }
  const { value } = await ElMessageBox.prompt(
    `请输入批量驳回 ${selectedIds.value.length} 条申请的原因`,
    '驳回原因',
    {
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
      type: 'warning',
      inputPlaceholder: '请输入驳回原因',
      inputType: 'textarea',
      inputValidator: (val: string) => {
        if (!val || !val.trim()) return '驳回原因不能为空'
        return true
      }
    }
  )
  await batchApproveCertifications({ ids: selectedIds.value, action: 'REJECT', rejectReason: value })
  ElMessage.success('批量驳回完成')
  fetchData()
}

onMounted(async () => {
  // 加载所有小区
  communityOptions.value = await getCascadeData('COMMUNITY')
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 16px; }
.search-card { margin-bottom: 16px; }
.search-form :deep(.el-form-item__label) { font-weight: 600; color: var(--color-text-primary); }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>