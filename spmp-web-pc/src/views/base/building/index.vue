<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="楼栋名称">
          <el-input v-model="queryParams.buildingName" placeholder="请输入楼栋名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="楼栋编号">
          <el-input v-model="queryParams.buildingCode" placeholder="请输入楼栋编号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="所属小区">
          <el-select v-model="queryParams.communityId" placeholder="全部" clearable filterable>
            <el-option v-for="c in communityOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼栋类型">
          <el-select v-model="queryParams.buildingType" placeholder="全部" clearable>
            <el-option label="住宅" value="RESIDENTIAL" />
            <el-option label="商业" value="COMMERCIAL" />
            <el-option label="车库" value="GARAGE" />
            <el-option label="混合" value="MIXED" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="启用" :value="0" />
            <el-option label="停用" :value="1" />
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
          <span>楼栋列表</span>
          <div>
            <el-button type="primary" v-permission="'base:building:create'" @click="handleAdd">新增楼栋</el-button>
            <el-button type="success" v-permission="'base:building:import'" @click="importVisible = true">Excel 导入</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" row-key="id">
        <el-table-column prop="buildingName" label="楼栋名称" min-width="120" />
        <el-table-column prop="buildingCode" label="楼栋编号" width="120" />
        <el-table-column prop="communityName" label="所属小区" width="140" />
        <el-table-column prop="aboveGroundFloors" label="地上层数" width="90" align="center" />
        <el-table-column prop="undergroundFloors" label="地下层数" width="90" align="center" />
        <el-table-column prop="unitsPerFloor" label="每层户数" width="90" align="center" />
        <el-table-column prop="buildingType" label="楼栋类型" width="100">
          <template #default="{ row }">{{ buildingTypeMap[row.buildingType] || row.buildingType }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">{{ row.status === 0 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="'base:building:edit'" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 0 ? 'warning' : 'success'" v-permission="'base:building:edit'" @click="handleToggleStatus(row)">
              {{ row.status === 0 ? '停用' : '启用' }}
            </el-button>
            <el-button link type="danger" v-permission="'base:building:delete'" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pagination" v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
        :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData" @current-change="fetchData" />
    </el-card>

    <BuildingForm ref="formRef" :community-options="communityOptions" @success="fetchData" />

    <!-- Excel 导入对话框 -->
    <el-dialog v-model="importVisible" title="Excel 导入楼栋" width="500px" destroy-on-close>
      <div style="margin-bottom: 16px;">
        <el-button type="primary" link @click="handleDownloadTemplate">下载导入模板</el-button>
      </div>
      <el-upload :auto-upload="false" :limit="1" accept=".xlsx,.xls" :on-change="handleFileChange">
        <template #trigger><el-button type="primary">选择文件</el-button></template>
      </el-upload>
      <div v-if="importResult" style="margin-top: 16px;">
        <el-alert :title="`导入完成：成功 ${importResult.successCount} 条，失败 ${importResult.failCount} 条`"
          :type="importResult.failCount > 0 ? 'warning' : 'success'" show-icon :closable="false" />
        <div v-if="importResult.errorMessages?.length" style="margin-top: 8px; max-height: 200px; overflow-y: auto;">
          <p v-for="(msg, i) in importResult.errorMessages" :key="i" style="color: #f56c6c; font-size: 12px;">{{ msg }}</p>
        </div>
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
import { listBuildings, deleteBuilding, changeBuildingStatus, importBuildings, downloadBuildingTemplate } from '@/api/base/building'
import type { BuildingPageItem } from '@/api/base/building'
import type { ImportResult } from '@/api/base/community'
import type { CascadeItem } from '@/api/base/cascade'
import { getCascadeData } from '@/api/base/cascade'
import { useBaseStore } from '@/store/modules/base'
import BuildingForm from './BuildingForm.vue'

const baseStore = useBaseStore()
const loading = ref(false)
const tableData = ref<BuildingPageItem[]>([])
const total = ref(0)
const formRef = ref()
const communityOptions = ref<CascadeItem[]>([])

const buildingTypeMap: Record<string, string> = {
  RESIDENTIAL: '住宅', COMMERCIAL: '商业', GARAGE: '车库', MIXED: '混合', OTHER: '其他'
}

const importVisible = ref(false)
const importing = ref(false)
const importFile = ref<File | null>(null)
const importResult = ref<ImportResult | null>(null)

const queryParams = reactive({
  buildingName: '',
  buildingCode: '',
  communityId: undefined as number | undefined,
  buildingType: undefined as string | undefined,
  status: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10
})

async function fetchData() {
  loading.value = true
  try {
    const res: any = await listBuildings(queryParams)
    tableData.value = res.data || res.list || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

function handleSearch() { queryParams.pageNum = 1; fetchData() }
function handleReset() {
  Object.assign(queryParams, { buildingName: '', buildingCode: '', communityId: undefined, buildingType: undefined, status: undefined })
  handleSearch()
}
function handleAdd() { formRef.value?.open() }
function handleEdit(row: BuildingPageItem) { formRef.value?.open(row) }

async function handleDelete(row: BuildingPageItem) {
  await ElMessageBox.confirm(`确认删除楼栋「${row.buildingName}」？`, '提示', { type: 'warning' })
  await deleteBuilding(row.id)
  ElMessage.success('删除成功')
  baseStore.clearCache(); fetchData()
}

async function handleToggleStatus(row: BuildingPageItem) {
  const newStatus = row.status === 0 ? 1 : 0
  const action = newStatus === 1 ? '停用' : '启用'
  await ElMessageBox.confirm(`确认${action}楼栋「${row.buildingName}」？`, '提示', { type: 'warning' })
  await changeBuildingStatus(row.id, newStatus)
  ElMessage.success(`${action}成功`)
  baseStore.clearCache(); fetchData()
}

function handleFileChange(file: UploadFile) { importFile.value = file.raw || null; importResult.value = null }

async function handleDownloadTemplate() {
  const res: any = await downloadBuildingTemplate()
  const url = window.URL.createObjectURL(new Blob([res]))
  const link = document.createElement('a')
  link.href = url; link.download = '楼栋导入模板.xlsx'; link.click()
  window.URL.revokeObjectURL(url)
}

async function handleImport() {
  if (!importFile.value) return
  importing.value = true
  try {
    importResult.value = await importBuildings(importFile.value)
    ElMessage.success('导入完成')
    baseStore.clearCache(); fetchData()
  } finally { importing.value = false }
}

onMounted(async () => {
  // 加载所有小区（不按片区过滤，供搜索栏使用）
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
