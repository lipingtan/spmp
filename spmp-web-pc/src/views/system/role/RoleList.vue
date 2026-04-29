<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="角色名称">
          <el-input v-model="queryParams.roleName" placeholder="请输入角色名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="queryParams.roleCode" placeholder="请输入角色编码" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="启用" :value="0" />
            <el-option label="禁用" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>角色列表</span>
          <el-button type="primary" @click="handleAdd">新增角色</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" row-key="id">
        <el-table-column prop="roleName" label="角色名称" width="140" />
        <el-table-column prop="roleCode" label="角色编码" width="160" />
        <el-table-column prop="dataPermissionLevel" label="数据权限" width="120" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">{{ row.status === 0 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleAssignMenu(row)">分配菜单</el-button>
            <el-button link type="primary" @click="handleDataPerm(row)">数据权限</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pagination" v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
        :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData" @current-change="fetchData" />
    </el-card>

    <RoleForm ref="formRef" @success="fetchData" />
    <MenuAssign ref="menuAssignRef" />
    <DataPermissionConfig ref="dataPermRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listRoles, deleteRole } from '@/api/role'
import type { RolePageItem } from '@/api/role'
import RoleForm from './RoleForm.vue'
import MenuAssign from './MenuAssign.vue'
import DataPermissionConfig from './DataPermissionConfig.vue'

const loading = ref(false)
const tableData = ref<RolePageItem[]>([])
const total = ref(0)
const formRef = ref()
const menuAssignRef = ref()
const dataPermRef = ref()

const queryParams = reactive({ roleName: '', roleCode: '', status: undefined as number | undefined, pageNum: 1, pageSize: 10 })

async function fetchData() {
  loading.value = true
  try {
    const res: any = await listRoles(queryParams)
    tableData.value = res.data || res.list || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

function handleSearch() { queryParams.pageNum = 1; fetchData() }
function handleReset() { queryParams.roleName = ''; queryParams.roleCode = ''; queryParams.status = undefined; handleSearch() }
function handleAdd() { formRef.value?.open() }
function handleEdit(row: RolePageItem) { formRef.value?.open(row) }
function handleAssignMenu(row: RolePageItem) { menuAssignRef.value?.open(row.id) }
function handleDataPerm(row: RolePageItem) { dataPermRef.value?.open(row.id, row.dataPermissionLevel) }

async function handleDelete(row: RolePageItem) {
  await ElMessageBox.confirm(`确认删除角色「${row.roleName}」？`, '提示', { type: 'warning' })
  await deleteRole(row.id)
  ElMessage.success('删除成功')
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
