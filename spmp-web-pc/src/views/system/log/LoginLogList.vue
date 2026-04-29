<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="登录IP">
          <el-input v-model="queryParams.loginIp" placeholder="请输入IP" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="结果">
          <el-select v-model="queryParams.loginResult" placeholder="全部" clearable>
            <el-option label="成功" :value="0" />
            <el-option label="失败" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header><span>登录日志</span></template>
      <el-table v-loading="loading" :data="tableData">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="loginIp" label="登录IP" width="140" />
        <el-table-column prop="loginLocation" label="登录地点" width="140" />
        <el-table-column prop="browser" label="浏览器" width="120" />
        <el-table-column prop="os" label="操作系统" width="120" />
        <el-table-column prop="loginResult" label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.loginResult === 0 ? 'success' : 'danger'" size="small">{{ row.loginResult === 0 ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failReason" label="失败原因" min-width="160" />
        <el-table-column prop="loginTime" label="登录时间" width="180" />
      </el-table>
      <el-pagination class="pagination" v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
        :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData" @current-change="fetchData" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { listLoginLogs } from '@/api/log'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({ username: '', loginIp: '', loginResult: undefined as number | undefined, pageNum: 1, pageSize: 10 })

async function fetchData() {
  loading.value = true
  try {
    const res: any = await listLoginLogs(queryParams)
    tableData.value = res.data || res.list || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

function handleSearch() { queryParams.pageNum = 1; fetchData() }
function handleReset() { queryParams.username = ''; queryParams.loginIp = ''; queryParams.loginResult = undefined; handleSearch() }

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 16px; }
.search-card { margin-bottom: 16px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
