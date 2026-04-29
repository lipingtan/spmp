<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="标题">
          <el-input v-model="queryParams.title" placeholder="公告标题" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryParams.noticeType" clearable style="min-width: 140px">
            <el-option label="全部" value="" />
            <el-option label="通知公告" value="NORMAL" />
            <el-option label="紧急公告" value="EMERGENCY" />
            <el-option label="活动公告" value="ACTIVITY" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" clearable style="min-width: 140px">
            <el-option label="全部" value="" />
            <el-option label="待审批" value="PENDING_APPROVAL" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="已撤回" value="WITHDRAWN" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleCreate">发布公告</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>公告列表</template>
      <el-table v-loading="loading" :data="tableData" row-key="id">
        <el-table-column prop="title" label="标题" min-width="220">
          <template #default="{ row }">
            <el-tag v-if="row.topFlag === 1" type="warning" size="small" style="margin-right: 6px">置顶</el-tag>
            <span>{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="noticeType" label="类型" width="110">
          <template #default="{ row }">
            {{ typeMap[row.noticeType] || row.noticeType }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="170" />
        <el-table-column prop="expireTime" label="过期时间" width="170" />
        <el-table-column label="是否过期" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.expired" type="info" size="small">已过期</el-tag>
            <el-tag v-else type="success" size="small">有效</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goStats(row)">统计</el-button>
            <el-button link type="warning" v-if="row.status === 'PUBLISHED'" @click="handleWithdraw(row)">撤回</el-button>
            <el-button link type="primary" v-if="row.status === 'PUBLISHED'" @click="handleRepush(row)">重推</el-button>
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

    <el-dialog v-model="repushVisible" title="重推公告" width="500px" destroy-on-close>
      <el-form :model="repushForm" label-width="100px">
        <el-form-item label="幂等键" required>
          <el-input v-model="repushForm.bizSerialNo" placeholder="业务流水号，用于幂等控制" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="repushForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="repushVisible = false">取消</el-button>
        <el-button type="primary" :loading="repushing" @click="submitRepush">确认重推</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listNotices, withdrawNotice, repushNotice } from '@/api/notice/notice'
import type { NoticeListVO } from '@/api/notice/notice'

const router = useRouter()
const loading = ref(false)
const tableData = ref<NoticeListVO[]>([])
const total = ref(0)

const typeMap: Record<string, string> = {
  NORMAL: '通知公告',
  EMERGENCY: '紧急公告',
  ACTIVITY: '活动公告'
}

const statusMap: Record<string, string> = {
  PENDING_APPROVAL: '待审批',
  PUBLISHED: '已发布',
  WITHDRAWN: '已撤回'
}

function statusTag(status: string): string {
  const map: Record<string, string> = {
    PENDING_APPROVAL: 'warning',
    PUBLISHED: 'success',
    WITHDRAWN: 'info'
  }
  return map[status] || 'info'
}

const queryParams = reactive({
  title: '',
  noticeType: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const repushVisible = ref(false)
const repushing = ref(false)
const currentRepushId = ref<number>(0)
const repushForm = reactive({ bizSerialNo: '', remark: '' })

async function fetchData() {
  loading.value = true
  try {
    const res: any = await listNotices(queryParams)
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
  queryParams.noticeType = ''
  queryParams.status = ''
  queryParams.pageNum = 1
  fetchData()
}

function handleCreate() {
  router.push('/notice/create')
}

function goStats(row: NoticeListVO) {
  router.push(`/notice/detail/${row.id}`)
}

async function handleWithdraw(row: NoticeListVO) {
  try {
    await ElMessageBox.confirm(`确认撤回公告「${row.title}」？撤回后业主端将无法再查看。`, '提示', { type: 'warning' })
    await withdrawNotice(row.id, '管理端撤回')
    ElMessage.success('撤回成功')
    fetchData()
  } catch {
    /* user cancelled */
  }
}

function handleRepush(row: NoticeListVO) {
  currentRepushId.value = row.id
  repushForm.bizSerialNo = `repush-${row.id}-${Date.now()}`
  repushForm.remark = ''
  repushVisible.value = true
}

async function submitRepush() {
  if (!repushForm.bizSerialNo) {
    ElMessage.warning('请填写幂等键')
    return
  }
  repushing.value = true
  try {
    await repushNotice(currentRepushId.value, { bizSerialNo: repushForm.bizSerialNo, remark: repushForm.remark })
    ElMessage.success('重推已提交')
    repushVisible.value = false
  } finally {
    repushing.value = false
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
