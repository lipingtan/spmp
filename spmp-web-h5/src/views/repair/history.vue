<!--
  H5 维修人员端 — 历史工单列表
-->
<template>
  <div class="repair-history">
    <van-nav-bar title="历史工单" left-arrow @click-left="goBack" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-card v-for="item in orders" :key="item.id" class="order-card">
          <template #title>
            <div class="card-header">
              <span class="order-no">{{ item.orderNo }}</span>
              <van-tag type="success" size="medium">{{ statusMap[item.status] || item.status }}</van-tag>
            </div>
          </template>
          <template #desc>
            <div class="card-desc">{{ item.description }}</div>
            <div class="card-info">{{ item.addressDesc }}</div>
            <div class="card-time">{{ item.actualCompleteTime || item.createTime }}</div>
          </template>
        </van-card>
        <van-empty v-if="!loading && orders.length === 0" description="暂无历史工单" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { listHistoryOrders } from '@/api/repair'
import type { WorkOrderSimpleVO } from '@/api/repair'

const router = useRouter()
const statusMap: Record<string, string> = { COMPLETED: '已完成', CANCELLED: '已取消', FORCE_CLOSED: '已关闭' }
const orders = ref<WorkOrderSimpleVO[]>([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)
const pageSize = 10

function goBack() { router.back() }

async function fetchOrders(reset = false) {
  if (reset) {
    pageNum.value = 1
    orders.value = []
    finished.value = false
  }
  try {
    const res: any = await listHistoryOrders({ pageNum: pageNum.value, pageSize })
    const list = res.data || res.list || []
    if (reset) orders.value = list
    else orders.value.push(...list)
    if (list.length < pageSize) finished.value = true
    pageNum.value++
  } catch {
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function loadMore() { fetchOrders() }
function onRefresh() { fetchOrders(true) }
</script>

<style scoped>
.repair-history { min-height: 100vh; background: #f5f5f5; }
.order-card { margin: 8px 12px; border-radius: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.order-no { font-weight: 600; font-size: 14px; }
.card-desc { color: #666; font-size: 13px; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-info { font-size: 12px; color: #999; margin-top: 4px; }
.card-time { font-size: 12px; color: #999; margin-top: 4px; }
</style>
