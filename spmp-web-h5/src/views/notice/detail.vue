<!--
  H5 公告详情 — 访问时自动标记已读
-->
<template>
  <div class="notice-detail">
    <van-nav-bar title="公告详情" left-arrow @click-left="goBack" />
    <div v-if="loading" class="loading">
      <van-loading size="24px">加载中...</van-loading>
    </div>
    <div v-else-if="detail" class="detail-body">
      <div class="title-row">
        <van-tag v-if="detail.topFlag === 1" type="warning" plain size="medium">置顶</van-tag>
        <van-tag :type="typeTag(detail.noticeType)" size="medium">{{ typeMap[detail.noticeType] || detail.noticeType }}</van-tag>
        <van-tag v-if="detail.expired" type="default" size="medium">已过期</van-tag>
      </div>
      <h2 class="title">{{ detail.title }}</h2>
      <div class="meta">发布时间：{{ detail.publishTime || '-' }}</div>
      <div v-if="detail.expireTime" class="meta">过期时间：{{ detail.expireTime }}</div>
      <div class="content" v-html="safeContent"></div>
    </div>
    <van-empty v-else description="公告不存在或已撤回" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNoticeDetail } from '@/api/notice'
import type { H5NoticeDetailVO } from '@/api/notice'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const detail = ref<H5NoticeDetailVO | null>(null)

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

const safeContent = computed(() => detail.value?.content || '')

function goBack() {
  router.back()
}

async function loadDetail() {
  const id = Number(route.params.id)
  if (!id) {
    detail.value = null
    loading.value = false
    return
  }
  loading.value = true
  try {
    detail.value = await getNoticeDetail(id)
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDetail()
})

watch(() => route.params.id, () => {
  loadDetail()
})
</script>

<style scoped>
.notice-detail {
  background: #fff;
  min-height: 100vh;
}
.loading {
  padding: 32px;
  text-align: center;
}
.detail-body {
  padding: 16px;
}
.title-row {
  display: flex;
  gap: 6px;
  align-items: center;
  margin-bottom: 12px;
}
.title {
  font-size: 18px;
  line-height: 1.5;
  font-weight: 700;
  color: #323233;
  margin: 0 0 8px 0;
}
.meta {
  font-size: 12px;
  color: #969799;
  margin-bottom: 4px;
}
.content {
  margin-top: 16px;
  font-size: 14px;
  line-height: 1.75;
  color: #323233;
  word-break: break-word;
}
.content :deep(img) {
  max-width: 100%;
  height: auto;
}
</style>
