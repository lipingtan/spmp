<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="片区名称">
          <el-input v-model="queryParams.districtName" placeholder="请输入片区名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="片区编码">
          <el-input v-model="queryParams.districtCode" placeholder="请输入片区编码" clearable @keyup.enter="handleSearch" />
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

    <!-- 操作栏 + 表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>片区列表</span>
          <el-button type="primary" v-permission="'base:district:create'" @click="handleAdd">新增片区</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" row-key="id">
        <el-table-column prop="districtName" label="片区名称" min-width="140" />
        <el-table-column prop="districtCode" label="片区编码" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="communityCount" label="下属小区数" width="120" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="'base:district:edit'" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 0 ? 'warning' : 'success'" v-permission="'base:district:edit'" @click="handleToggleStatus(row)">
              {{ row.status === 0 ? '停用' : '启用' }}
            </el-button>
            <el-button link type="danger" v-permission="'base:district:delete'" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑对话框 -->
    <DistrictForm ref="formRef" @success="fetchData" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listDistricts, deleteDistrict, changeDistrictStatus } from '@/api/base/district'
import type { DistrictPageItem } from '@/api/base/district'
import DistrictForm from './DistrictForm.vue'

const loading = ref(false)
const tableData = ref<DistrictPageItem[]>([])
const total = ref(0)
const formRef = ref()

const queryParams = reactive({
  districtName: '',
  districtCode: '',
  status: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10
})

async function fetchData() {
  loading.value = true
  try {
    const res: any = await listDistricts(queryParams)
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
  queryParams.districtName = ''
  queryParams.districtCode = ''
  queryParams.status = undefined
  handleSearch()
}

function handleAdd() {
  formRef.value?.open()
}

function handleEdit(row: DistrictPageItem) {
  formRef.value?.open(row)
}

async function handleDelete(row: DistrictPageItem) {
  await ElMessageBox.confirm(`确认删除片区「${row.districtName}」？`, '提示', { type: 'warning' })
  await deleteDistrict(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

async function handleToggleStatus(row: DistrictPageItem) {
  const newStatus = row.status === 0 ? 1 : 0
  const action = newStatus === 1 ? '停用' : '启用'
  await ElMessageBox.confirm(`确认${action}片区「${row.districtName}」？`, '提示', { type: 'warning' })
  await changeDistrictStatus(row.id, newStatus)
  ElMessage.success(`${action}成功`)
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 16px; }
.search-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
