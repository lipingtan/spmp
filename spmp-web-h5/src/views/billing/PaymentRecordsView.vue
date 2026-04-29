<template>
  <div class="payment-records-page">
    <van-nav-bar title="缴费记录" left-arrow @click-left="$router.back()" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <van-empty v-if="!loading && list.length === 0" description="暂无缴费记录" />

        <div v-for="item in list" :key="item.id" class="record-item" @click="goDetail(item.id)">
          <div class="record-header">
            <span class="payment-no">{{ item.paymentNo }}</span>
            <span class="amount">¥{{ item.totalAmount?.toFixed(2) }}</span>
          </div>
          <div class="record-footer">
            <span class="pay-method">{{ item.payMethodName || item.payMethod }}</span>
            <span class="paid-time">{{ item.paidTime || item.createTime }}</span>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getPaymentHistory, type PaymentVO } from '@/api/billing'

const router = useRouter()
const list = ref<PaymentVO[]>([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)
const pageSize = 10

async function fetchData(reset = false) {
  if (reset) {
    pageNum.value = 1
    finished.value = false
    list.value = []
  }
  try {
    const res = await getPaymentHistory({ pageNum: pageNum.value, pageSize })
    // 后端返回 Result<PageResult>，拦截器因 PageResult 含 total 字段返回整个 PageResult 对象
    const pageResult = res as any
    const records: PaymentVO[] = pageResult?.data || []
    const total = pageResult?.total || 0
    list.value = reset ? records : [...list.value, ...records]
    finished.value = list.value.length >= total
    pageNum.value++
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function onLoad() {
  fetchData()
}

function onRefresh() {
  fetchData(true)
}

function goDetail(id: number) {
  router.push(`/billing/detail/${id}`)
}
</script>

<style scoped>
.record-item {
  background: #fff;
  margin: 8px 12px;
  border-radius: 8px;
  padding: 14px 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.payment-no {
  font-size: 13px;
  color: #646566;
}
.amount {
  font-size: 18px;
  font-weight: bold;
  color: #07c160;
}
.record-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #969799;
}
</style>
