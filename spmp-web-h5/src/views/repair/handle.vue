<!--
  H5 维修人员端 — 工单处理页
  - 接单/完成/转派
-->
<template>
  <div class="repair-handle">
    <van-nav-bar title="工单处理" left-arrow @click-left="goBack" />

    <div v-if="orderInfo" class="handle-content">
      <van-cell-group inset>
        <van-cell title="工单编号" :value="orderInfo.orderNo" />
        <van-cell title="报修类型" :value="orderInfo.orderType" />
        <van-cell title="状态" :value="statusMap[orderInfo.status] || orderInfo.status" />
        <van-cell title="地址" :value="orderAddressText" />
        <van-cell title="描述" :label="orderInfo.description" />
      </van-cell-group>

      <div class="action-area" v-if="orderInfo.status === 'PENDING_ACCEPT'">
        <van-button type="primary" block round @click="handleAccept" :loading="accepting">接单</van-button>
      </div>

      <div class="action-area" v-if="orderInfo.status === 'IN_PROGRESS'">
        <van-button type="primary" block round @click="showCompletePopup = true">完成维修</van-button>
        <van-button type="warning" block round plain style="margin-top: 8px" @click="showTransferPopup = true">转派</van-button>
      </div>
    </div>

    <van-popup v-model:show="showCompletePopup" position="bottom" round :style="{ maxHeight: '85%' }">
      <div class="popup-content">
        <div class="popup-title">完成维修</div>
        <van-form @submit="submitComplete">
          <van-cell-group inset>
            <van-field v-model="completeForm.repairDescription" label="处理说明" type="textarea" rows="3" placeholder="请描述处理情况" required :rules="[{ required: true, message: '请填写处理说明' }]" />
            <van-field v-model="completeForm.repairDuration" label="维修时长(分钟)" type="digit" placeholder="请输入" required :rules="[{ required: true, message: '请输入维修时长' }]" />
            <van-field name="repairImages" label="维修照片">
              <template #input>
                <van-uploader
                  v-model="repairFileList"
                  :max-count="5"
                  :max-size="5 * 1024 * 1024"
                  accept=".jpg,.jpeg,.png,.gif,.webp"
                  multiple
                  :after-read="onRepairAfterRead"
                  @delete="onRepairDeleteImage"
                />
              </template>
            </van-field>
          </van-cell-group>
          <div style="padding: 16px">
            <van-button type="primary" block round native-type="submit" :loading="completing">提交</van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <van-popup v-model:show="showTransferPopup" position="bottom" round :style="{ maxHeight: '50%' }">
      <div class="popup-content">
        <div class="popup-title">转派工单</div>
        <van-field v-model="transferReason" label="转派原因" type="textarea" rows="3" placeholder="请说明转派原因" required />
        <van-button type="warning" block round style="margin-top: 16px" :loading="transferring" @click="submitTransfer">确认转派</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import type { UploaderFileListItem } from 'vant'
import { acceptWorkOrder, completeWorkOrder, transferWorkOrder, getRepairOrderDetail } from '@/api/repair'
import type { WorkOrderDetailVO } from '@/api/workorder'
import { uploadFile, validateFile } from '@/api/common/upload'

const route = useRoute()
const router = useRouter()
const orderInfo = ref<WorkOrderDetailVO | null>(null)
const orderAddressText = computed(() => {
  if (!orderInfo.value) {
    return ''
  }
  const parts = [
    orderInfo.value.communityName,
    orderInfo.value.buildingName,
    orderInfo.value.houseName
  ].filter(Boolean)
  return parts.join(' ') || '-'
})

const statusMap: Record<string, string> = {
  PENDING_DISPATCH: '待派发', PENDING_ACCEPT: '待接单', IN_PROGRESS: '处理中',
  PENDING_VERIFY: '待验收', COMPLETED: '已完成', CANCELLED: '已取消', FORCE_CLOSED: '已关闭'
}

const accepting = ref(false)
const showCompletePopup = ref(false)
const completing = ref(false)
const completeForm = reactive({ repairDescription: '', repairDuration: '' })
const repairFileList = ref<UploaderFileListItem[]>([])
const repairUploadedUrls = ref<string[]>([])

const showTransferPopup = ref(false)
const transferring = ref(false)
const transferReason = ref('')

function goBack() { router.back() }

async function fetchOrder() {
  const id = Number(route.params.id)
  if (!id) return
  try {
    const res: any = await getRepairOrderDetail(id)
    orderInfo.value = res
  } catch { /* ignore */ }
}

async function handleAccept() {
  if (!orderInfo.value) return
  accepting.value = true
  try {
    await acceptWorkOrder(orderInfo.value.id)
    showToast('接单成功')
    fetchOrder()
  } finally {
    accepting.value = false
  }
}

async function submitComplete() {
  if (!orderInfo.value) return
  completing.value = true
  try {
    await completeWorkOrder(orderInfo.value.id, {
      repairDescription: completeForm.repairDescription,
      repairDuration: Number(completeForm.repairDuration),
      imageUrls: repairUploadedUrls.value.length > 0 ? repairUploadedUrls.value : undefined
    })
    showToast('已完成维修')
    showCompletePopup.value = false
    repairFileList.value = []
    repairUploadedUrls.value = []
    fetchOrder()
  } finally {
    completing.value = false
  }
}

async function submitTransfer() {
  if (!orderInfo.value) return
  if (!transferReason.value) {
    showToast('请填写转派原因')
    return
  }
  transferring.value = true
  try {
    await transferWorkOrder(orderInfo.value.id, transferReason.value)
    showToast('已转派')
    showTransferPopup.value = false
    goBack()
  } finally {
    transferring.value = false
  }
}

async function onRepairAfterRead(item: UploaderFileListItem | UploaderFileListItem[]) {
  const items = Array.isArray(item) ? item : [item]
  for (const file of items) {
    if (!file.file) continue
    const error = validateFile(file.file)
    if (error) {
      showToast(error)
      const idx = repairFileList.value.indexOf(file)
      if (idx > -1) repairFileList.value.splice(idx, 1)
      continue
    }
    file.status = 'uploading'
    file.message = '上传中'
    try {
      const url = await uploadFile(file.file!)
      file.status = 'done'
      file.message = ''
      repairUploadedUrls.value.push(url)
    } catch {
      file.status = 'failed'
      file.message = '上传失败'
    }
  }
}

function onRepairDeleteImage(_item: UploaderFileListItem, detail: { index: number }) {
  if (repairUploadedUrls.value.length > detail.index) {
    repairUploadedUrls.value.splice(detail.index, 1)
  }
}

onMounted(() => { fetchOrder() })
</script>

<style scoped>
.repair-handle { min-height: 100vh; background: #f5f5f5; }
.handle-content { padding-bottom: 100px; }
.action-area { position: fixed; bottom: 0; left: 0; right: 0; padding: 12px 16px; background: #fff; box-shadow: 0 -2px 8px rgba(0,0,0,0.06); }
.popup-content { padding: 16px; }
.popup-title { font-size: 16px; font-weight: 600; text-align: center; margin-bottom: 12px; }
</style>
