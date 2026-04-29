<!--
  H5 工单详情页（业主端）
  - 基本信息 + 状态步骤条
  - 验收/评价/催单/取消操作
-->
<template>
  <div class="detail-page" v-loading="loading">
    <van-nav-bar title="工单详情" left-arrow @click-left="goBack" />

    <div v-if="detail" class="detail-content">
      <van-steps :active="activeStep" class="status-steps">
        <van-step>待派发</van-step>
        <van-step>待接单</van-step>
        <van-step>处理中</van-step>
        <van-step>待验收</van-step>
        <van-step>已完成</van-step>
      </van-steps>

      <van-cell-group inset class="info-group">
        <van-cell title="工单编号" :value="detail.orderNo" />
        <van-cell title="报修类型" :value="orderTypeMap[detail.orderType] || detail.orderType" />
        <van-cell title="报修人" :value="detail.reporterName" />
        <van-cell title="维修人员" :value="detail.repairUserName || '未派发'" />
        <van-cell title="创建时间" :value="detail.createTime" />
        <van-cell title="问题描述" :label="detail.description" />
      </van-cell-group>

      <van-cell-group inset class="info-group" v-if="detail.reportImages && detail.reportImages.length > 0" title="报修图片">
        <div class="image-list">
          <van-image
            v-for="(url, idx) in detail.reportImages"
            :key="idx"
            :src="url"
            fit="cover"
            width="80"
            height="80"
            radius="4"
            @click="previewImage(detail.reportImages, idx)"
          />
        </div>
      </van-cell-group>

      <van-cell-group inset class="info-group" v-if="detail.materials && detail.materials.length > 0" title="维修材料">
        <van-cell v-for="m in detail.materials" :key="m.id" :title="m.materialName" :value="`${m.quantity}${m.unit || ''} × ¥${m.unitPrice}`" :label="`总价：¥${m.totalPrice}`" />
      </van-cell-group>

      <van-cell-group inset class="info-group" v-if="detail.evaluation" title="评价信息">
        <van-cell title="评分">
          <template #value>
            <van-rate :model-value="detail.evaluation.score" disabled size="16" />
          </template>
        </van-cell>
        <van-cell v-if="detail.evaluation.content" title="评价" :value="detail.evaluation.content" />
      </van-cell-group>

      <van-cell-group inset class="info-group" title="操作记录">
        <van-cell v-for="log in detail.logs" :key="log.id" :title="actionMap[log.action] || log.action" :label="log.remark || ''">
          <template #value>
            <span class="log-time">{{ log.operateTime }}</span>
          </template>
        </van-cell>
      </van-cell-group>

      <div class="action-bar" v-if="showActions">
        <van-button v-if="detail.status === 'PENDING_VERIFY'" type="primary" block round @click="showVerifyPopup = true">验收</van-button>
        <van-button v-if="detail.status === 'PENDING_VERIFY'" type="warning" block round plain @click="handleUrge" style="margin-top: 8px">催单</van-button>
        <van-button v-if="detail.status === 'COMPLETED' && !detail.evaluation" type="primary" block round @click="showEvaluatePopup = true">评价</van-button>
        <van-button v-if="canCancel" type="danger" block round plain @click="showCancelPopup = true" style="margin-top: 8px">取消工单</van-button>
      </div>
    </div>

    <van-popup v-model:show="showVerifyPopup" position="bottom" round :style="{ maxHeight: '80%' }">
      <div class="popup-content">
        <div class="popup-title">验收工单</div>
        <van-radio-group v-model="verifyResult" style="margin: 16px 0">
          <van-radio name="pass" style="margin-bottom: 8px">验收通过</van-radio>
          <van-radio name="reject">验收不通过</van-radio>
        </van-radio-group>
        <van-field v-if="verifyResult === 'pass'" v-model="verifyScore" label="评分" type="number" placeholder="1-5星" />
        <van-field v-if="verifyResult === 'reject'" v-model="verifyRejectReason" label="不通过原因" type="textarea" rows="2" placeholder="请说明原因" required />
        <van-field v-if="verifyResult === 'reject'" name="rejectImages" label="不通过照片">
          <template #input>
            <van-uploader
              v-model="rejectFileList"
              :max-count="5"
              :max-size="5 * 1024 * 1024"
              accept=".jpg,.jpeg,.png,.gif,.webp"
              multiple
              :after-read="onRejectAfterRead"
              @delete="onRejectDeleteImage"
            />
          </template>
        </van-field>
        <van-button type="primary" block round style="margin-top: 16px" :loading="verifyLoading" @click="submitVerify">提交</van-button>
      </div>
    </van-popup>

    <van-popup v-model:show="showEvaluatePopup" position="bottom" round :style="{ maxHeight: '70%' }">
      <div class="popup-content">
        <div class="popup-title">评价工单</div>
        <div style="padding: 16px 0">
          <van-rate v-model="evaluateScore" size="28" />
        </div>
        <van-field v-model="evaluateContent" label="评价内容" type="textarea" rows="2" placeholder="请输入评价内容" />
        <van-button type="primary" block round style="margin-top: 16px" :loading="evaluateLoading" @click="submitEvaluate">提交评价</van-button>
      </div>
    </van-popup>

    <van-popup v-model:show="showCancelPopup" position="bottom" round :style="{ maxHeight: '60%' }">
      <div class="popup-content">
        <div class="popup-title">取消工单</div>
        <van-field v-model="cancelReason" label="取消原因" type="textarea" rows="2" placeholder="请输入取消原因" required />
        <van-button type="danger" block round style="margin-top: 16px" :loading="cancelLoading" @click="submitCancel">确认取消</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showImagePreview } from 'vant'
import type { UploaderFileListItem } from 'vant'
import { getWorkOrderDetail, verifyWorkOrder, evaluateWorkOrder, urgeWorkOrder, cancelWorkOrder } from '@/api/workorder'
import type { WorkOrderDetailVO } from '@/api/workorder'
import { uploadFile, validateFile } from '@/api/common/upload'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<WorkOrderDetailVO | null>(null)

const orderTypeMap: Record<string, string> = {
  WATER_ELECTRIC: '水电维修',
  DOOR_WINDOW: '门窗维修',
  PIPELINE: '管道疏通',
  PUBLIC_FACILITY: '公共设施',
  OTHER: '其他'
}

const actionMap: Record<string, string> = {
  CREATE: '创建', DISPATCH: '派发', ACCEPT: '接单', COMPLETE: '完成',
  VERIFY_PASS: '验收通过', VERIFY_REJECT: '验收不通过', TRANSFER: '转派',
  CANCEL: '取消', FORCE_CLOSE: '强制关闭', URGE: '催单'
}

const activeStep = computed(() => {
  if (!detail.value) return 0
  const map: Record<string, number> = {
    PENDING_DISPATCH: 0, PENDING_ACCEPT: 1, IN_PROGRESS: 2, PENDING_VERIFY: 3, COMPLETED: 4
  }
  return map[detail.value.status] ?? 0
})

const showActions = computed(() => {
  if (!detail.value) return false
  return ['PENDING_VERIFY', 'COMPLETED', 'PENDING_DISPATCH', 'PENDING_ACCEPT', 'IN_PROGRESS'].includes(detail.value.status)
})

const canCancel = computed(() => {
  if (!detail.value) return false
  return ['PENDING_DISPATCH', 'PENDING_ACCEPT', 'IN_PROGRESS'].includes(detail.value.status)
})

const showVerifyPopup = ref(false)
const verifyResult = ref('pass')
const verifyScore = ref('')
const verifyRejectReason = ref('')
const verifyLoading = ref(false)
const rejectFileList = ref<UploaderFileListItem[]>([])
const rejectUploadedUrls = ref<string[]>([])

const showEvaluatePopup = ref(false)
const evaluateScore = ref(5)
const evaluateContent = ref('')
const evaluateLoading = ref(false)

const showCancelPopup = ref(false)
const cancelReason = ref('')
const cancelLoading = ref(false)

function goBack() {
  router.back()
}

function previewImage(images: string[], startIndex: number) {
  showImagePreview({ images, startPosition: startIndex })
}

async function fetchDetail() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  try {
    detail.value = await getWorkOrderDetail(id)
  } finally {
    loading.value = false
  }
}

async function submitVerify() {
  if (!detail.value) return
  verifyLoading.value = true
  try {
    const passed = verifyResult.value === 'pass'
    if (!passed && !verifyRejectReason.value) {
      showToast('请填写不通过原因')
      return
    }
    await verifyWorkOrder(detail.value.id, {
      passed,
      rejectReason: passed ? undefined : verifyRejectReason.value,
      rejectImageUrls: passed ? undefined : (rejectUploadedUrls.value.length > 0 ? rejectUploadedUrls.value : undefined),
      score: passed && verifyScore.value ? Number(verifyScore.value) : undefined
    })
    showToast('验收成功')
    showVerifyPopup.value = false
    rejectFileList.value = []
    rejectUploadedUrls.value = []
    fetchDetail()
  } finally {
    verifyLoading.value = false
  }
}

async function handleUrge() {
  if (!detail.value) return
  await urgeWorkOrder(detail.value.id)
  showToast('催单成功')
  fetchDetail()
}

async function submitEvaluate() {
  if (!detail.value) return
  evaluateLoading.value = true
  try {
    await evaluateWorkOrder(detail.value.id, {
      score: evaluateScore.value,
      content: evaluateContent.value
    })
    showToast('评价成功')
    showEvaluatePopup.value = false
    fetchDetail()
  } finally {
    evaluateLoading.value = false
  }
}

async function submitCancel() {
  if (!detail.value) return
  if (!cancelReason.value) {
    showToast('请填写取消原因')
    return
  }
  cancelLoading.value = true
  try {
    await cancelWorkOrder(detail.value.id, { cancelReason: cancelReason.value })
    showToast('已取消')
    showCancelPopup.value = false
    fetchDetail()
  } finally {
    cancelLoading.value = false
  }
}

async function onRejectAfterRead(item: UploaderFileListItem | UploaderFileListItem[]) {
  const items = Array.isArray(item) ? item : [item]
  for (const file of items) {
    if (!file.file) continue
    const error = validateFile(file.file)
    if (error) {
      showToast(error)
      const idx = rejectFileList.value.indexOf(file)
      if (idx > -1) rejectFileList.value.splice(idx, 1)
      continue
    }
    file.status = 'uploading'
    file.message = '上传中'
    try {
      const url = await uploadFile(file.file!)
      file.status = 'done'
      file.message = ''
      rejectUploadedUrls.value.push(url)
    } catch {
      file.status = 'failed'
      file.message = '上传失败'
    }
  }
}

function onRejectDeleteImage(_item: UploaderFileListItem, detail: { index: number }) {
  if (rejectUploadedUrls.value.length > detail.index) {
    rejectUploadedUrls.value.splice(detail.index, 1)
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: #f5f5f5;
}
.detail-content {
  padding-bottom: 100px;
}
.status-steps {
  margin: 12px;
  border-radius: 8px;
  overflow: hidden;
}
.info-group {
  margin: 12px 0;
}
.image-list {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  flex-wrap: wrap;
}
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
}
.popup-content {
  padding: 16px;
}
.popup-title {
  font-size: 16px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 12px;
}
.log-time {
  font-size: 12px;
  color: #999;
}
</style>
