<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="房屋编号">
          <el-input v-model="queryParams.houseCode" placeholder="请输入房屋编号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="所属小区">
          <el-select v-model="selectedCommunityId" placeholder="全部" clearable filterable @change="onCommunityChange">
            <el-option v-for="c in communityOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属楼栋">
          <el-select v-model="selectedBuildingId" placeholder="全部" clearable filterable @change="onBuildingChange" :disabled="!selectedCommunityId">
            <el-option v-for="b in buildingOptions" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属单元">
          <el-select v-model="queryParams.unitId" placeholder="全部" clearable filterable :disabled="!selectedBuildingId">
            <el-option v-for="u in unitOptions" :key="u.id" :label="u.name" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房屋状态">
          <el-select v-model="queryParams.houseStatus" placeholder="全部" clearable>
            <el-option label="空置" value="VACANT" />
            <el-option label="已入住" value="OCCUPIED" />
            <el-option label="已出租" value="RENTED" />
            <el-option label="装修中" value="RENOVATING" />
          </el-select>
        </el-form-item>
        <el-form-item label="房屋类型">
          <el-select v-model="queryParams.houseType" placeholder="全部" clearable>
            <el-option label="住宅" value="RESIDENCE" />
            <el-option label="商铺" value="SHOP" />
            <el-option label="车位" value="PARKING" />
            <el-option label="办公" value="OFFICE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>房屋列表</span>
          <div>
            <el-button type="primary" v-permission="'base:house:create'" @click="handleAdd">新增房屋</el-button>
            <el-button type="success" v-permission="'base:house:import'" @click="importVisible = true">Excel 导入</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" row-key="id">
        <el-table-column prop="houseCode" label="房屋编号" width="120" />
        <el-table-column prop="communityName" label="所属小区" width="130" />
        <el-table-column prop="buildingName" label="所属楼栋" width="100" />
        <el-table-column prop="unitName" label="所属单元" width="100" />
        <el-table-column prop="floor" label="楼层" width="70" align="center" />
        <el-table-column prop="buildingArea" label="建筑面积(㎡)" width="120" align="right" />
        <el-table-column prop="usableArea" label="使用面积(㎡)" width="120" align="right" />
        <el-table-column prop="houseStatus" label="房屋状态" width="100">
          <template #default="{ row }">
            <el-tag :type="houseStatusType[row.houseStatus]" size="small">{{ houseStatusMap[row.houseStatus] || row.houseStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="houseType" label="房屋类型" width="90">
          <template #default="{ row }">{{ houseTypeMap[row.houseType] || row.houseType }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="'base:house:edit'" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" v-permission="'base:house:edit'" @click="handleChangeStatus(row)">状态变更</el-button>
            <el-button link type="info" @click="handleViewLogs(row)">变更历史</el-button>
            <el-button link type="danger" v-permission="'base:house:delete'" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pagination" v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
        :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData" @current-change="fetchData" />
    </el-card>

    <HouseForm ref="formRef" @success="fetchData" />

    <!-- 状态变更对话框 -->
    <el-dialog v-model="statusVisible" title="房屋状态变更" width="400px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="当前状态">
          <el-tag :type="houseStatusType[currentRow?.houseStatus || '']" size="small">{{ houseStatusMap[currentRow?.houseStatus || ''] }}</el-tag>
        </el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="newStatus" placeholder="请选择新状态" style="width: 100%">
            <el-option v-for="(label, value) in houseStatusMap" :key="value" :label="label" :value="value"
              :disabled="value === currentRow?.houseStatus" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusVisible = false">取消</el-button>
        <el-button type="primary" :loading="statusSubmitting" @click="submitStatusChange">确定</el-button>
      </template>
    </el-dialog>

    <!-- 状态变更历史对话框 -->
    <el-dialog v-model="logsVisible" title="状态变更历史" width="700px" destroy-on-close>
      <el-table :data="statusLogs" v-loading="logsLoading">
        <el-table-column prop="oldStatus" label="变更前状态" width="120">
          <template #default="{ row }">{{ row.oldStatus ? houseStatusMap[row.oldStatus] : '—' }}</template>
        </el-table-column>
        <el-table-column prop="newStatus" label="变更后状态" width="120">
          <template #default="{ row }">{{ houseStatusMap[row.newStatus] || row.newStatus }}</template>
        </el-table-column>
        <el-table-column prop="changeTime" label="变更时间" width="180" />
        <el-table-column prop="operatorId" label="操作人ID" width="100" />
      </el-table>
    </el-dialog>

    <!-- Excel 导入对话框 -->
    <el-dialog v-model="importVisible" title="Excel 导入房屋" width="500px" destroy-on-close>
      <div style="margin-bottom: 16px;">
        <el-button type="primary" link @click="handleDownloadTemplate">下载导入模板</el-button>
      </div>
      <el-upload :auto-upload="false" :limit="1" accept=".xlsx,.xls" :on-change="handleFileChange">
        <template #trigger><el-button type="primary">选择文件</el-button></template>
      </el-upload>
      <div v-if="importResult" style="margin-top: 16px;">
        <el-alert :title="`导入完成：成功 ${importResult.successCount} 条，失败 ${importResult.failCount} 条`"
          :type="importResult.failCount > 0 ? 'warning' : 'success'" show-icon :closable="false" />
      </div>
      <template #footer>
        <el-button @click="importVisible = false">关闭</el-button>
        <el-button type="primary" :loading="importing" :disabled="!importFile" @click="handleImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadFile } from 'element-plus'
import { listHouses, deleteHouse, changeHouseStatus, getHouseStatusLogs, importHouses, downloadHouseTemplate } from '@/api/base/house'
import type { HousePageItem, HouseStatusLog } from '@/api/base/house'
import type { ImportResult } from '@/api/base/community'
import type { CascadeItem } from '@/api/base/cascade'
import { getCascadeData } from '@/api/base/cascade'
import { useBaseStore } from '@/store/modules/base'
import HouseForm from './HouseForm.vue'

const baseStore = useBaseStore()
const loading = ref(false)
const tableData = ref<HousePageItem[]>([])
const total = ref(0)
const formRef = ref()

// 三级联动选项
const communityOptions = ref<CascadeItem[]>([])
const buildingOptions = ref<CascadeItem[]>([])
const unitOptions = ref<CascadeItem[]>([])
const selectedCommunityId = ref<number>()
const selectedBuildingId = ref<number>()

const houseStatusMap: Record<string, string> = { VACANT: '空置', OCCUPIED: '已入住', RENTED: '已出租', RENOVATING: '装修中' }
const houseStatusType: Record<string, string> = { VACANT: 'info', OCCUPIED: 'success', RENTED: 'warning', RENOVATING: '' }
const houseTypeMap: Record<string, string> = { RESIDENCE: '住宅', SHOP: '商铺', PARKING: '车位', OFFICE: '办公', OTHER: '其他' }

// 状态变更
const statusVisible = ref(false)
const statusSubmitting = ref(false)
const currentRow = ref<HousePageItem | null>(null)
const newStatus = ref('')

// 状态变更历史
const logsVisible = ref(false)
const logsLoading = ref(false)
const statusLogs = ref<HouseStatusLog[]>([])

// 导入
const importVisible = ref(false)
const importing = ref(false)
const importFile = ref<File | null>(null)
const importResult = ref<ImportResult | null>(null)

const queryParams = reactive({
  houseCode: '',
  unitId: undefined as number | undefined,
  houseStatus: undefined as string | undefined,
  houseType: undefined as string | undefined,
  pageNum: 1,
  pageSize: 10
})

async function fetchData() {
  loading.value = true
  try {
    const params: any = { ...queryParams }
    if (selectedBuildingId.value) params.buildingId = selectedBuildingId.value
    if (selectedCommunityId.value) params.communityId = selectedCommunityId.value
    const res: any = await listHouses(params)
    tableData.value = res.data || res.list || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

async function onCommunityChange(val: number | undefined) {
  selectedBuildingId.value = undefined
  queryParams.unitId = undefined
  buildingOptions.value = []
  unitOptions.value = []
  if (val) {
    buildingOptions.value = await baseStore.loadBuildings(val)
  }
}

async function onBuildingChange(val: number | undefined) {
  queryParams.unitId = undefined
  unitOptions.value = []
  if (val) {
    unitOptions.value = await baseStore.loadUnits(val)
  }
}

function handleSearch() { queryParams.pageNum = 1; fetchData() }
function handleReset() {
  Object.assign(queryParams, { houseCode: '', unitId: undefined, houseStatus: undefined, houseType: undefined })
  selectedCommunityId.value = undefined
  selectedBuildingId.value = undefined
  buildingOptions.value = []
  unitOptions.value = []
  handleSearch()
}
function handleAdd() { formRef.value?.open() }
function handleEdit(row: HousePageItem) { formRef.value?.open(row) }

async function handleDelete(row: HousePageItem) {
  await ElMessageBox.confirm(`确认删除房屋「${row.houseCode}」？`, '提示', { type: 'warning' })
  await deleteHouse(row.id)
  ElMessage.success('删除成功')
  baseStore.clearCache(); fetchData()
}

function handleChangeStatus(row: HousePageItem) {
  currentRow.value = row
  newStatus.value = ''
  statusVisible.value = true
}

async function submitStatusChange() {
  if (!newStatus.value || !currentRow.value) return
  statusSubmitting.value = true
  try {
    await changeHouseStatus(currentRow.value.id, { newStatus: newStatus.value })
    ElMessage.success('状态变更成功')
    statusVisible.value = false
    baseStore.clearCache(); fetchData()
  } finally { statusSubmitting.value = false }
}

async function handleViewLogs(row: HousePageItem) {
  logsVisible.value = true
  logsLoading.value = true
  try {
    statusLogs.value = await getHouseStatusLogs(row.id)
  } finally { logsLoading.value = false }
}

function handleFileChange(file: UploadFile) { importFile.value = file.raw || null; importResult.value = null }

async function handleDownloadTemplate() {
  const res: any = await downloadHouseTemplate()
  const url = window.URL.createObjectURL(new Blob([res]))
  const link = document.createElement('a')
  link.href = url; link.download = '房屋导入模板.xlsx'; link.click()
  window.URL.revokeObjectURL(url)
}

async function handleImport() {
  if (!importFile.value) return
  importing.value = true
  try {
    importResult.value = await importHouses(importFile.value)
    ElMessage.success('导入完成')
    baseStore.clearCache(); fetchData()
  } finally { importing.value = false }
}

onMounted(async () => {
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
