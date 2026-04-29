<template>
  <div class="billing-page">
    <van-sticky>
      <van-tabs v-model:active="activeTab" @change="onTabChange">
        <van-tab title="&#20840;&#37096;" name="" />
        <van-tab title="&#24453;&#32564;&#36153;" name="UNPAID" />
        <van-tab title="&#24050;&#32564;&#36153;" name="PAID" />
        <van-tab title="&#24050;&#36894;&#26399;" name="OVERDUE" />
      </van-tabs>
    </van-sticky>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="&#27809;&#26377;&#26356;&#22810;&#20102;"
        @load="onLoad"
      >
        <van-empty v-if="!loading && list.length === 0" description="&#26242;&#26080;&#36134;&#21333;" />

        <div v-for="item in list" :key="item.id" class="bill-card" @click="goDetail(item.id)">
          <div class="bill-top">
            <span class="fee">{{ item.feeTypeName || feeTypeName(item.feeType) }}</span>
            <van-tag :type="tagType(item.status)">{{ item.statusName || statusName(item.status) }}</van-tag>
          </div>
          <div class="amount">&#65509;{{ Number(item.payAmount || 0).toFixed(2) }}</div>
          <div class="meta">
            <span>&#36134;&#26399;&#65306;{{ item.billingPeriod }}</span>
            <span>&#25130;&#27490;&#65306;{{ item.dueDate }}</span>
          </div>
          <van-button
            v-if="item.status === 'UNPAID' || item.status === 'OVERDUE'"
            type="primary"
            size="small"
            class="pay-btn"
            @click.stop="handlePay(item.id)"
          >
            &#31435;&#21363;&#32564;&#36153;
          </van-button>
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getMyBills, createPayment, paymentCallback, type H5BillListVO } from '@/api/billing'

const router = useRouter()
const activeTab = ref('')
const list = ref<H5BillListVO[]>([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const pageNum = ref(1)
const pageSize = 10

const feeTypeName = (v?: string) =>
  ({
    PROPERTY_FEE: '物业费',
    PARKING_FEE: '停车费',
    WATER_FEE: '水费',
    ELECTRICITY_FEE: '电费',
    GAS_FEE: '燃气费'
  } as any)[v || ''] || v || '-'

const statusName = (v?: string) =>
  ({
    UNPAID: '待缴费',
    PAYING: '待支付',
    PAID: '已缴费',
    OVERDUE: '已逾期',
    REDUCED: '已减免',
    REFUNDED: '已退款',
    CANCELLED: '已取消'
  } as any)[v || ''] || v || '-'

const tagType = (v?: string) => {
  if (v === 'PAID') return 'success'
  if (v === 'OVERDUE') return 'danger'
  if (v === 'UNPAID' || v === 'PAYING') return 'warning'
  return 'default'
}

const fetchData = async (reset = false) => {
  if (reset) {
    pageNum.value = 1
    finished.value = false
    list.value = []
  }
  const res: any = await getMyBills({
    status: activeTab.value || undefined,
    pageNum: pageNum.value,
    pageSize
  })
  const rows: H5BillListVO[] = res?.data || []
  const total = Number(res?.total || 0)
  list.value = reset ? rows : list.value.concat(rows)
  finished.value = list.value.length >= total
  pageNum.value += 1
}

const onLoad = async () => {
  try {
    await fetchData(false)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onRefresh = async () => {
  try {
    await fetchData(true)
  } finally {
    refreshing.value = false
    loading.value = false
  }
}

const onTabChange = async () => {
  loading.value = true
  await fetchData(true)
  loading.value = false
}

const goDetail = (id: number) => {
  router.push(`/billing/detail/${id}`)
}

const handlePay = async (id: number) => {
  try {
    const paymentId = (await createPayment([id])) as unknown as number
    const mockPaySign = import.meta.env.VITE_MOCK_PAY_SIGN || ''
    if (mockPaySign) {
      await paymentCallback(paymentId, mockPaySign)
    }
    showToast('支付已提交')
    await fetchData(true)
  } catch (e: any) {
    showToast(e?.message || '支付失败')
  }
}

loading.value = true
fetchData(true).finally(() => {
  loading.value = false
})
</script>

<style scoped>
.billing-page { min-height: 100%; background: #f7f8fa; }
.bill-card { background: #fff; margin: 10px 12px; border-radius: 10px; padding: 12px; }
.bill-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.fee { font-size: 14px; font-weight: 600; }
.amount { font-size: 24px; color: #ee0a24; font-weight: 700; margin-bottom: 6px; }
.meta { display: flex; justify-content: space-between; color: #969799; font-size: 12px; }
.pay-btn { margin-top: 8px; width: 100%; }
</style>
