<template>
  <div class="bill-detail-page">
    <van-nav-bar title="账单详情" left-arrow @click-left="$router.back()" />

    <van-loading v-if="loading" type="spinner" class="page-loading" />

    <template v-else-if="bill">
      <!-- 金额卡片 -->
      <div class="amount-card" :class="statusClass">
        <div class="status-name">{{ bill.statusName }}</div>
        <div class="pay-amount">¥{{ bill.payAmount?.toFixed(2) }}</div>
        <div class="amount-desc">应收 ¥{{ bill.amount?.toFixed(2) }}
          <span v-if="bill.reduceAmount > 0">，已减免 ¥{{ bill.reduceAmount?.toFixed(2) }}</span>
        </div>
      </div>

      <!-- 账单信息 -->
      <van-cell-group inset title="账单信息" class="info-group">
        <van-cell title="账单编号" :value="bill.billNo" />
        <van-cell title="费用类型" :value="bill.feeTypeName" />
        <van-cell title="账期" :value="bill.billingPeriod" />
        <van-cell title="截止日期" :value="bill.dueDate" />
        <van-cell v-if="bill.houseFullName" title="房屋" :value="bill.houseFullName" />
        <van-cell v-if="bill.usageAmount" title="用量" :value="bill.usageAmount + ' 单位'" />
        <van-cell v-if="bill.unitPrice" title="单价" :value="'¥' + bill.unitPrice" />
        <van-cell v-if="bill.houseArea" title="房屋面积" :value="bill.houseArea + ' ㎡'" />
        <van-cell v-if="bill.paidTime" title="缴费时间" :value="bill.paidTime" />
        <van-cell title="创建时间" :value="bill.createTime" />
        <van-cell v-if="bill.remark" title="备注" :value="bill.remark" />
      </van-cell-group>

      <!-- 减免记录 -->
      <van-cell-group v-if="bill.reduceRecords?.length" inset title="减免记录" class="info-group">
        <van-cell
          v-for="r in bill.reduceRecords"
          :key="r.id"
          :title="'减免 ¥' + r.reduceAmount?.toFixed(2)"
          :label="r.reason"
          :value="r.approveTime"
        />
      </van-cell-group>

      <!-- 支付记录 -->
      <van-cell-group v-if="bill.payments?.length" inset title="支付记录" class="info-group">
        <van-cell
          v-for="p in bill.payments"
          :key="p.id"
          :title="'¥' + p.totalAmount?.toFixed(2)"
          :label="p.payMethodName || p.payMethod"
          :value="p.statusName"
        />
      </van-cell-group>
    </template>

    <!-- 支付按钮 -->
    <van-submit-bar
      v-if="bill && (bill.status === 'UNPAID' || bill.status === 'OVERDUE')"
      :price="(bill.payAmount || 0) * 100"
      button-text="立即缴费"
      :loading="paying"
      @submit="handlePay"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'
import { getBillDetail, createPayment, paymentCallback, type H5BillDetailVO } from '@/api/billing'

const route = useRoute()
const router = useRouter()
const enableAutoMockCallback = import.meta.env.VITE_ENABLE_AUTO_MOCK_PAY_CALLBACK === 'true'
const mockPaySign = import.meta.env.VITE_MOCK_PAY_SIGN || ''
const loading = ref(true)
const paying = ref(false)
const bill = ref<H5BillDetailVO & { payments?: any[]; reduceRecords?: any[] } | null>(null)

const statusClass = computed(() => {
  const s = bill.value?.status
  if (s === 'PAID') return 'status-paid'
  if (s === 'OVERDUE') return 'status-overdue'
  if (s === 'CANCELLED' || s === 'REDUCED' || s === 'REFUNDED') return 'status-closed'
  return 'status-unpaid'
})

async function fetchDetail() {
  loading.value = true
  try {
    const res = await getBillDetail(Number(route.params.id))
    // 拦截器已解包 Result，res 本身就是 H5BillDetailVO
    bill.value = res as any
  } catch {
    showToast('加载失败')
    router.back()
  } finally {
    loading.value = false
  }
}

async function handlePay() {
  if (!bill.value) return
  paying.value = true
  const toast = showLoadingToast({ message: '支付处理中...', forbidClick: true, duration: 0 })
  try {
    const paymentId = await createPayment([bill.value.id]) as unknown as number
    if (enableAutoMockCallback) {
      if (!mockPaySign) {
        throw new Error('缺少 VITE_MOCK_PAY_SIGN，无法执行自动回调')
      }
      await paymentCallback(paymentId, mockPaySign)
      showToast({ type: 'success', message: '模拟支付成功' })
    } else {
      showToast({ type: 'success', message: '支付申请已提交，请等待支付结果' })
    }
    closeToast()
    await fetchDetail()
  } catch (error: any) {
    closeToast()
    showToast(error?.message || '支付处理失败')
  } finally {
    paying.value = false
  }
}

onMounted(() => { fetchDetail() })
</script>

<style scoped>
.page-loading {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}
.amount-card {
  padding: 32px 20px 24px;
  text-align: center;
  color: #fff;
}
.status-unpaid { background: linear-gradient(135deg, #ff976a, #ff6034); }
.status-overdue { background: linear-gradient(135deg, #ee0a24, #c00); }
.status-paid { background: linear-gradient(135deg, #07c160, #05a14e); }
.status-closed { background: linear-gradient(135deg, #969799, #646566); }
.status-name { font-size: 14px; opacity: 0.9; margin-bottom: 8px; }
.pay-amount { font-size: 36px; font-weight: bold; margin-bottom: 6px; }
.amount-desc { font-size: 13px; opacity: 0.85; }
.info-group { margin-top: 12px; }
</style>
