<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="header-row">
          <span>公告统计</span>
          <el-button link @click="goBack">返回列表</el-button>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="公告ID">{{ noticeId }}</el-descriptions-item>
        <el-descriptions-item label="目标人数">{{ stats.targetCount }}</el-descriptions-item>
        <el-descriptions-item label="已读人数">{{ stats.readCount }}</el-descriptions-item>
        <el-descriptions-item label="未读人数">{{ stats.unreadCount }}</el-descriptions-item>
        <el-descriptions-item label="已读率">
          <el-progress :percentage="readRatePercent" :stroke-width="14" :color="readRateColor" />
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="header-row">
          <span>未读用户</span>
          <el-button size="small" type="primary" @click="openRepush">重推未读用户</el-button>
        </div>
      </template>
      <el-table v-loading="loading" :data="tableData">
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="userName" label="用户姓名" width="160" />
        <el-table-column prop="communityId" label="小区ID" width="120" />
        <el-table-column prop="buildingId" label="楼栋ID" width="120" />
      </el-table>
      <el-pagination
        class="pagination"
        v-model:current-page="page.pageNum"
        v-model:page-size="page.pageSize"
        :total="page.total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchUnread"
        @current-change="fetchUnread"
      />
    </el-card>

    <el-dialog v-model="repushVisible" title="重推未读用户" width="500px" destroy-on-close>
      <el-form :model="repushForm" label-width="100px">
        <el-form-item label="幂等键" required>
          <el-input v-model="repushForm.bizSerialNo" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getReadStats, listUnreadUsers, repushNotice } from '@/api/notice/notice'
import type { NoticeReadStatsVO, NoticeUnreadUserVO } from '@/api/notice/notice'

const route = useRoute()
const router = useRouter()
const noticeId = Number(route.params.id)

const stats = ref<NoticeReadStatsVO>({ announcementId: noticeId, targetCount: 0, readCount: 0, unreadCount: 0, readRate: 0 })
const readRatePercent = computed(() => Math.round((stats.value.readRate || 0) * 10000) / 100)
const readRateColor = computed(() => {
  if (readRatePercent.value >= 80) return '#67c23a'
  if (readRatePercent.value >= 50) return '#e6a23c'
  return '#f56c6c'
})

const loading = ref(false)
const tableData = ref<NoticeUnreadUserVO[]>([])
const page = reactive({ pageNum: 1, pageSize: 20, total: 0 })

const repushVisible = ref(false)
const repushing = ref(false)
const repushForm = reactive({ bizSerialNo: '', remark: '' })

async function fetchStats() {
  const res: any = await getReadStats(noticeId)
  stats.value = res.data
}

async function fetchUnread() {
  loading.value = true
  try {
    const res: any = await listUnreadUsers(noticeId, page.pageNum, page.pageSize)
    tableData.value = res.data || []
    page.total = res.total || 0
  } finally {
    loading.value = false
  }
}

function openRepush() {
  repushForm.bizSerialNo = `repush-${noticeId}-${Date.now()}`
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
    await repushNotice(noticeId, { bizSerialNo: repushForm.bizSerialNo, remark: repushForm.remark })
    ElMessage.success('重推已提交')
    repushVisible.value = false
    await fetchStats()
  } finally {
    repushing.value = false
  }
}

function goBack() {
  router.push('/notice/list')
}

onMounted(async () => {
  await Promise.all([fetchStats(), fetchUnread()])
})
</script>

<style scoped>
.page-container {
  padding: 16px;
}
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.table-card {
  margin-top: 16px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
