<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="标题">
          <el-input v-model="queryParams.title" placeholder="公告标题" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>待审批公告</template>
      <el-table v-loading="loading" :data="tableData" row-key="id">
        <el-table-column prop="title" label="标题" min-width="240" />
        <el-table-column prop="noticeType" label="类型" width="110">
          <template #default="{ row }">{{ typeMap[row.noticeType] || row.noticeType }}</template>
        </el-table-column>
        <el-table-column prop="publishTime" label="预计发布时间" width="170" />
        <el-table-column prop="expireTime" label="过期时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" @click="openApprove(row, 'APPROVE')">通过</el-button>
            <el-button link type="danger" @click="openApprove(row, 'REJECT')">驳回</el-button>
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

    <el-dialog v-model="approveVisible" :title="approveForm.action === 'APPROVE' ? '审批通过' : '审批驳回'" width="500px" destroy-on-close>
      <el-form :model="approveForm" label-width="80px">
        <el-form-item label="备注">
          <el-input v-model="approveForm.remark" type="textarea" :rows="3" placeholder="请填写审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button :type="approveForm.action === 'APPROVE' ? 'success' : 'danger'" :loading="submitting" @click="submit">
          确认{{ approveForm.action === 'APPROVE' ? '通过' : '驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listApprovals, approveNotice } from '@/api/notice/notice'
import type { NoticeListVO } from '@/api/notice/notice'

const loading = ref(false)
const tableData = ref<NoticeListVO[]>([])
const total = ref(0)

const typeMap: Record<string, string> = {
  NORMAL: '通知公告',
  EMERGENCY: '紧急公告',
  ACTIVITY: '活动公告'
}

const queryParams = reactive({ title: '', pageNum: 1, pageSize: 10 })

const approveVisible = ref(false)
const submitting = ref(false)
const currentId = ref<number>(0)
const approveForm = reactive<{ action: 'APPROVE' | 'REJECT'; remark: string }>({ action: 'APPROVE', remark: '' })

async function fetchData() {
  loading.value = true
  try {
    const res: any = await listApprovals(queryParams)
    tableData.value = res.data || []
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
  queryParams.title = ''
  queryParams.pageNum = 1
  fetchData()
}

function openApprove(row: NoticeListVO, action: 'APPROVE' | 'REJECT') {
  currentId.value = row.id
  approveForm.action = action
  approveForm.remark = ''
  approveVisible.value = true
}

async function submit() {
  submitting.value = true
  try {
    await approveNotice(currentId.value, approveForm)
    ElMessage.success(approveForm.action === 'APPROVE' ? '审批通过' : '已驳回')
    approveVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page-container {
  padding: 16px;
}
.search-card {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
