<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="菜单名称">
          <el-input v-model="queryParams.menuName" placeholder="请输入菜单名称" clearable @keyup.enter="fetchData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="启用" :value="0" />
            <el-option label="禁用" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="queryParams.menuName = ''; queryParams.status = undefined; fetchData()">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>菜单列表</span>
          <el-button type="primary" @click="handleAdd()">新增菜单</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="menuTree" row-key="id" default-expand-all :tree-props="{ children: 'children' }">
        <el-table-column prop="menuName" label="菜单名称" min-width="180" />
        <el-table-column prop="icon" label="图标" width="80">
          <template #default="{ row }">{{ row.icon || '-' }}</template>
        </el-table-column>
        <el-table-column prop="menuType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.menuType === 'D' ? '' : row.menuType === 'M' ? 'success' : 'warning'" size="small">
              {{ getMenuTypeLabel(row.menuType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="permission" label="权限标识" min-width="160" />
        <el-table-column prop="path" label="路由路径" min-width="160" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">{{ row.status === 0 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.menuType !== 'B'" link type="primary" @click="handleAdd(row.id)">新增</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <MenuForm ref="formRef" @success="fetchData" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuTree, deleteMenu } from '@/api/menu'
import type { MenuTreeItem } from '@/api/menu'
import MenuForm from './MenuForm.vue'

const loading = ref(false)
const menuTree = ref<MenuTreeItem[]>([])
const formRef = ref()
const queryParams = reactive({ menuName: '', status: undefined as number | undefined })
const menuTypeLabelMap: Record<'D' | 'M' | 'B', string> = {
  D: '目录',
  M: '菜单',
  B: '按钮'
}

function getMenuTypeLabel(menuType: string): string {
  return menuTypeLabelMap[menuType as keyof typeof menuTypeLabelMap] || menuType
}

async function fetchData() {
  loading.value = true
  try { menuTree.value = await getMenuTree(queryParams.menuName, queryParams.status) }
  finally { loading.value = false }
}

function handleAdd(parentId?: number) { formRef.value?.open(undefined, parentId || 0) }
function handleEdit(row: MenuTreeItem) { formRef.value?.open(row) }

async function handleDelete(row: MenuTreeItem) {
  await ElMessageBox.confirm(`确认删除菜单「${row.menuName}」？`, '提示', { type: 'warning' })
  await deleteMenu(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 16px; }
.search-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
