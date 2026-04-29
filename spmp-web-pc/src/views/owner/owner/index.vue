<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="姓名">
          <el-input v-model="queryParams.ownerName" placeholder="请输入姓名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="小区">
          <el-select v-model="queryParams.communityId" placeholder="全部" clearable filterable @change="onCommunityChange">
            <el-option v-for="c in communityOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼栋">
          <el-select v-model="queryParams.buildingId" placeholder="全部" clearable filterable :disabled="!queryParams.communityId">
            <el-option v-for="b in buildingOptions" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="业主状态">
          <el-select v-model="queryParams.ownerStatus" placeholder="全部" clearable>
            <el-option label="未认证" value="UNCERTIFIED" />
            <el-option label="认证中" value="CERTIFYING" />
            <el-option label="已认证" value="CERTIFIED" />
            <el-option label="已停用" value="DISABLED" />
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
          <span>业主列表</span>
          <div>
            <el-button type="primary" v-if="hasPermission('owner:owner:add')" @click="handleAdd">新增业主</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" row-key="id">
        <el-table-column prop="ownerName" label="姓名" min-width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="goDetail(row.id)">{{ row.ownerName }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="phoneMasked" label="手机号" width="130" />
        <el-table-column prop="idCardMasked" label="身份证号" width="180" />
        <el-table-column prop="gender" label="性别" width="70" align="center">
          <template #default="{ row }">
            {{ genderText(row.gender) }}
          </template>
        </el-table-column>
        <el-table-column prop="ownerStatus" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.ownerStatus)" size="small">{{ statusText(row.ownerStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerSource" label="来源" width="100" align="center">
          <template #default="{ row }">
            {{ sourceText(row.ownerSource) }}
          </template>
        </el-table-column>
        <el-table-column prop="communityName" label="小区" min-width="120" show-overflow-tooltip />
        <el-table-column prop="buildingName" label="楼栋" width="100" />
        <el-table-column prop="unitName" label="单元" width="80" />
        <el-table-column prop="houseName" label="房屋" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-if="hasPermission('owner:owner:edit')" @click="handleEdit(row)">编辑</el-button>
            <el-button
              link
              :type="row.ownerStatus === 'DISABLED' ? 'success' : 'warning'"
              v-if="hasPermission('owner:owner:status') && row.ownerStatus !== 'UNCERTIFIED' && row.ownerStatus !== 'CERTIFYING'"
              @click="handleToggleStatus(row)"
            >
              {{ row.ownerStatus === 'DISABLED' ? '启用' : '停用' }}
            </el-button>
            <el-button link type="danger" v-if="hasPermission('owner:owner:delete')" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑弹窗（Task 22.2 创建，先预留引用） -->
    <OwnerForm ref="formRef" @success="fetchData" />
  </div>
</template>

<script setup lang="ts">
/**
 * 业主列表页面
 * 功能：搜索、分页列表、新增/编辑/删除/停用/启用操作
 */
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listOwners, deleteOwner, changeOwnerStatus } from '@/api/owner/owner'
import type { OwnerListVO } from '@/api/owner/owner'
import type { CascadeItem } from '@/api/base/cascade'
import { getCascadeData } from '@/api/base/cascade'
import { useUserStore } from '@/store/modules/user'
import { useBaseStore } from '@/store/modules/base'
import OwnerForm from './OwnerForm.vue'

const router = useRouter()
const userStore = useUserStore()
const baseStore = useBaseStore()

const loading = ref(false)
const tableData = ref<OwnerListVO[]>([])
const total = ref(0)
const formRef = ref()

/** 小区下拉选项 */
const communityOptions = ref<CascadeItem[]>([])
/** 楼栋下拉选项（依赖小区） */
const buildingOptions = ref<CascadeItem[]>([])

/** 查询参数 */
const queryParams = reactive({
  ownerName: '',
  phone: '',
  communityId: undefined as number | undefined,
  buildingId: undefined as number | undefined,
  ownerStatus: undefined as string | undefined,
  pageNum: 1,
  pageSize: 10
})

/** 权限检查 */
function hasPermission(perm: string): boolean {
  return userStore.hasPermission(perm)
}

/** 性别文本 */
function genderText(gender: number): string {
  const map: Record<number, string> = { 0: '未知', 1: '男', 2: '女' }
  return map[gender] ?? '未知'
}

/** 业主状态标签类型 */
function statusTagType(status: string): '' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<string, '' | 'success' | 'warning' | 'danger' | 'info'> = {
    UNCERTIFIED: 'info',
    CERTIFYING: 'warning',
    CERTIFIED: 'success',
    DISABLED: 'danger'
  }
  return map[status] ?? 'info'
}

/** 业主状态文本 */
function statusText(status: string): string {
  const map: Record<string, string> = {
    UNCERTIFIED: '未认证',
    CERTIFYING: '认证中',
    CERTIFIED: '已认证',
    DISABLED: '已停用'
  }
  return map[status] ?? status
}

/** 来源文本 */
function sourceText(source: string): string {
  const map: Record<string, string> = { ADMIN: '管理端录入', H5: 'H5 注册' }
  return map[source] ?? source
}

/** 加载列表数据 */
async function fetchData() {
  loading.value = true
  try {
    const res: any = await listOwners(queryParams)
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
  queryParams.ownerName = ''
  queryParams.phone = ''
  queryParams.communityId = undefined
  queryParams.buildingId = undefined
  queryParams.ownerStatus = undefined
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

/** 新增业主 */
function handleAdd() {
  formRef.value?.open()
}

/** 编辑业主 */
function handleEdit(row: OwnerListVO) {
  formRef.value?.open(row.id)
}

/** 跳转详情页 */
function goDetail(id: number) {
  router.push(`/owner/owners/${id}`)
}

/** 删除业主 */
async function handleDelete(row: OwnerListVO) {
  await ElMessageBox.confirm(`确认删除业主「${row.ownerName}」？删除后不可恢复。`, '提示', { type: 'warning' })
  await deleteOwner(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

/** 停用/启用业主 */
async function handleToggleStatus(row: OwnerListVO) {
  if (row.ownerStatus === 'DISABLED') {
    await changeOwnerStatus(row.id, { status: 'ENABLE' })
    ElMessage.success('启用成功')
    fetchData()
  } else {
    const { value } = await ElMessageBox.prompt(
      `确认停用业主「${row.ownerName}」？`,
      '停用确认',
      {
        confirmButtonText: '确认停用',
        cancelButtonText: '取消',
        type: 'warning',
        inputPlaceholder: '请输入停用原因（可选）',
        inputType: 'textarea'
      }
    )
    await changeOwnerStatus(row.id, { status: 'DISABLED', reason: value || undefined })
    ElMessage.success('停用成功')
    fetchData()
  }
}

onMounted(async () => {
  // 加载所有小区（不依赖片区）
  communityOptions.value = await getCascadeData('COMMUNITY')
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 16px; }
.search-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
