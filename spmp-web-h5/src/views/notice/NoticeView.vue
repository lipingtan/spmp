<!--
  公告 — 业主端我的公告列表
-->
<template>
  <div class="notice-page">
    <van-tabs v-model:active="activeType" @change="onTabChange" sticky>
      <van-tab title="全部" name="" />
      <van-tab title="通知公告" name="NORMAL" />
      <van-tab title="紧急公告" name="EMERGENCY" />
      <van-tab title="活动公告" name="ACTIVITY" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <div v-for="item in list" :key="item.id" class="notice-card" @click="goDetail(item.id)">
          <div class="card-header">
            <van-tag v-if="item.topFlag === 1" type="warning" plain size="medium" class="top-tag">置顶</van-tag>
            <van-tag :type="typeTag(item.noticeType)" size="medium">{{ typeMap[item.noticeType] || item.noticeType }}</van-tag>
            <van-tag v-if="!item.read" type="danger" size="medium" plain class="unread-tag">未读</van-tag>
          </div>
          <div class="card-title">{{ item.title }}</div>
          <div class="card-meta">
            <span>{{ item.publishTime || '' }}</span>
            <span v-if="item.expired" class="expired">已过期</span>
          </div>
        </div>
        <van-empty v-if="!loading && list.length === 0" description="暂无公告" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { listMyNotices } from '@/api/notice'
import type { H5NoticeListVO } from '@/api/notice'

const router = useRouter()
const activeType = ref('')
const list = ref<H5NoticeListVO[]>([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const page = reactive({ pageNum: 0, pageSize: 10 })

const typeMap: Record<string, string> = {
  NORMAL: '通知',
  EMERGENCY: '紧急',
  ACTIVITY: '活动'
}

type VanTagType = 'primary' | 'success' | 'warning' | 'danger' | 'default'
function typeTag(type: string): VanTagType {
  const map: Record<string, VanTagType> = {
    NORMAL: 'primary',
    EMERGENCY: 'danger',
    ACTIVITY: 'success'
  }
  return map[type] || 'primary'
}

async function loadMore() {
  if (loading.value || finished.value) return
  loading.value = true
  page.pageNum += 1
  try {
    const res: any = await listMyNotices({
      noticeType: activeType.value || undefined,
      pageNum: page.pageNum,
      pageSize: page.pageSize
    })
    const records = res.list || res.records || res.data || []
    list.value.push(...records)
    if (!records.length || list.value.length >= (res.total || 0)) {
      finished.value = true
    }
  } catch {
    finished.value = true
  } finally {
    loading.value = false
  }
}

async function onRefresh() {
  finished.value = false
  page.pageNum = 0
  list.value = []
  await loadMore()
  refreshing.value = false
}

async function onTabChange() {
  await onRefresh()
}

function goDetail(id: number) {
  router.push(`/notice/${id}`)
}

onMounted(() => {
  onRefresh()
})

onActivated(() => {
  if (!list.value.length) {
    onRefresh()
  }
})
</script>

<style scoped>
.notice-page {
  padding-bottom: 16px;
}
.notice-card {
  margin: 12px;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.card-header {
  display: flex;
  gap: 6px;
  align-items: center;
  margin-bottom: 6px;
}
.top-tag {
  font-weight: 600;
}
.unread-tag {
  margin-left: auto;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  line-height: 1.4;
  color: #323233;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #969799;
}
.expired {
  color: #c8c9cc;
}
</style>
