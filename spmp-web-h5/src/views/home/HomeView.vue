<!--
  H5 首页
  - 顶部欢迎语 + 小区名称（硬编码「阳光花园小区」）
  - 中部 van-grid 2×2 快捷入口卡片
  - 下部最新公告摘要（van-cell-group 显示「暂无公告」）
-->
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { showToast } from 'vant'
import { listMyNotices } from '@/api/notice'
import type { H5NoticeListVO } from '@/api/notice'
import { getProfile } from '@/api/owner'

const router = useRouter()
const userStore = useUserStore()
const latestNotices = ref<H5NoticeListVO[]>([])
const loadingNotices = ref(false)
const communityName = ref('我的小区')

/** 快捷入口配置 */
const shortcuts = [
  { text: '报修', icon: 'orders-o', path: '/workorder', needLogin: true },
  { text: '缴费', icon: 'balance-o', path: '/billing', needLogin: true },
  { text: '房产认证', icon: 'certificate', path: '/certify', needLogin: true },
  { text: '家庭成员', icon: 'friends-o', path: '/mine/family', needLogin: true },
  { text: '公告', icon: 'bell', path: '/notice', needLogin: false },
  { text: '访客预约', icon: 'shield-o', path: '', needLogin: true }
]

/** 点击快捷入口 */
function onShortcutClick(item: typeof shortcuts[number]) {
  if (!item.path) {
    showToast('功能开发中，敬请期待')
    return
  }
  // 需要登录但未登录，跳转登录页
  if (item.needLogin && !userStore.token) {
    router.push({ path: '/login', query: { redirect: item.path } })
    return
  }
  router.push(item.path)
}

function goNoticeDetail(id: number) {
  router.push(`/notice/${id}`)
}

function goNoticeList() {
  router.push('/notice')
}

async function loadLatestNotices() {
  loadingNotices.value = true
  try {
    const res: any = await listMyNotices({ pageNum: 1, pageSize: 3 })
    const records = res?.data || res?.list || res?.records || []
    latestNotices.value = records.slice(0, 3)
  } catch {
    latestNotices.value = []
  } finally {
    loadingNotices.value = false
  }
}

async function loadCommunityName() {
  if (!userStore.token) {
    communityName.value = '访客'
    return
  }
  try {
    const profile = await getProfile()
    communityName.value = profile.propertyBindings?.[0]?.communityName || '我的小区'
  } catch {
    communityName.value = '我的小区'
  }
}

onMounted(() => {
  loadCommunityName()
  loadLatestNotices()
})
</script>

<template>
  <div class="home-page">
    <!-- 欢迎区域 -->
    <div class="home-welcome">
      <p class="home-welcome__greeting">
        你好，{{ userStore.username || '业主' }}
      </p>
      <p class="home-welcome__community">{{ communityName }}</p>
    </div>

    <!-- 快捷入口 -->
    <div class="home-shortcuts">
      <van-grid :column-num="3" :gutter="10">
        <van-grid-item
          v-for="item in shortcuts"
          :key="item.text"
          :icon="item.icon"
          :text="item.text"
          @click="onShortcutClick(item)"
        />
      </van-grid>
    </div>

    <!-- 最新公告 -->
    <div class="home-notice">
      <van-cell-group title="最新公告">
        <van-loading v-if="loadingNotices" size="20px" class="notice-loading">加载中...</van-loading>
        <template v-else-if="latestNotices.length">
          <van-cell
            v-for="item in latestNotices"
            :key="item.id"
            :title="item.title"
            is-link
            @click="goNoticeDetail(item.id)"
          />
          <van-cell title="查看更多公告" is-link @click="goNoticeList" />
        </template>
        <van-cell v-else title="暂无公告" />
      </van-cell-group>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  padding: var(--spacing-lg);
}

.home-welcome {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, var(--color-brand-gradient-start), var(--color-brand-gradient-end));
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
  margin-bottom: var(--spacing-lg);
  color: #fff;
  box-shadow: var(--shadow-lg);
}

.home-welcome::after {
  content: '';
  position: absolute;
  right: -30%;
  top: -40%;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  filter: blur(4px);
}

.home-welcome__greeting {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: var(--spacing-xs);
}

.home-welcome__community {
  font-size: 14px;
  opacity: 0.85;
}

.home-shortcuts {
  margin-bottom: var(--spacing-lg);
}

.home-notice {
  margin-bottom: var(--spacing-lg);
}

.home-shortcuts :deep(.van-grid-item__content) {
  border-radius: var(--border-radius-md);
  background: var(--color-surface);
  box-shadow: var(--shadow-sm);
}

.home-shortcuts :deep(.van-grid-item__text) {
  color: var(--color-text-primary);
  font-weight: 600;
  letter-spacing: 0.2px;
  text-shadow: 0 1px 1px rgba(0, 0, 0, 0.08);
}

.home-notice :deep(.van-cell-group) {
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.notice-loading {
  display: flex;
  justify-content: center;
  padding: var(--spacing-md) 0;
}
</style>
