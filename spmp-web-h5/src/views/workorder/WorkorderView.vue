<!--
  H5 报修 — 业主端我的工单列表
  - 顶部状态 Tab 筛选
  - 工单卡片列表
  - 底部提交报修按钮
-->
<template>
  <div class="workorder-page">
    <van-tabs v-model:active="activeTab" @change="onTabChange" sticky>
      <van-tab title="全部" name="" />
      <van-tab title="待派发" name="PENDING_DISPATCH" />
      <van-tab title="处理中" name="IN_PROGRESS" />
      <van-tab title="待验收" name="PENDING_VERIFY" />
      <van-tab title="已完成" name="COMPLETED" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadMore"
      >
        <van-card
          v-for="item in orders"
          :key="item.id"
          class="order-card"
          @click="goDetail(item.id)"
        >
          <template #title>
            <div class="card-header">
              <span class="order-no">{{ item.orderNo }}</span>
              <van-tag :type="statusTagType(item.status)" size="medium">{{ statusMap[item.status] }}</van-tag>
            </div>
          </template>
          <template #desc>
            <div class="card-desc">{{ item.description }}</div>
            <div class="card-info">
              <span>{{ orderTypeMap[item.orderType] || item.orderType }}</span>
              <span>{{ item.addressDesc }}</span>
            </div>
            <div class="card-time">{{ item.createTime }}</div>
          </template>
        </van-card>
        <van-empty v-if="!loading && orders.length === 0" description="暂无工单" />
      </van-list>
    </van-pull-refresh>

    <van-button
      type="primary"
      icon="plus"
      class="create-btn"
      round
      @click="showCreate = true"
    >
      提交报修
    </van-button>

    <van-popup v-model:show="showCreate" position="bottom" round :style="{ height: '85%' }">
      <div class="create-popup">
        <div class="popup-header">
          <span>提交报修</span>
          <van-icon name="cross" @click="showCreate = false" />
        </div>
        <van-form @submit="submitCreate" class="create-form">
          <van-cell-group inset>
            <van-field name="orderType" label="报修类型" required :rules="[{ required: true, message: '请选择报修类型' }]">
              <template #input>
                <van-radio-group v-model="createForm.orderType" direction="horizontal">
                  <van-radio name="WATER_ELECTRIC">水电</van-radio>
                  <van-radio name="DOOR_WINDOW">门窗</van-radio>
                  <van-radio name="PIPELINE">管道</van-radio>
                  <van-radio name="PUBLIC_FACILITY">公共设施</van-radio>
                  <van-radio name="OTHER">其他</van-radio>
                </van-radio-group>
              </template>
            </van-field>
            <van-field name="addressType" label="地址类型" required :rules="[{ required: true, message: '请选择地址类型' }]">
              <template #input>
                <van-radio-group v-model="createForm.addressType" direction="horizontal">
                  <van-radio name="HOUSE">房屋</van-radio>
                  <van-radio name="PUBLIC">公共区域</van-radio>
                </van-radio-group>
              </template>
            </van-field>
            <van-field
              name="communityId"
              label="所属小区"
              required
              readonly
              is-link
              :rules="[{ required: true, message: '请选择所属小区' }]"
              :model-value="communityOptions.find((item) => item.id === createForm.communityId)?.name || ''"
              placeholder="请选择所属小区"
              @click="showCommunityPicker = true"
            />
            <van-field
              v-model="createForm.description"
              label="问题描述"
              type="textarea"
              rows="3"
              placeholder="请描述您的问题"
              required
              :rules="[{ required: true, message: '请填写问题描述' }]"
            />
            <van-field name="imageUrls" label="现场照片">
              <template #input>
                <van-uploader
                  v-model="uploadFileList"
                  :max-count="5"
                  :max-size="5 * 1024 * 1024"
                  accept=".jpg,.jpeg,.png,.gif,.webp"
                  multiple
                  :after-read="onAfterRead"
                  @delete="onDeleteImage"
                />
              </template>
            </van-field>
          </van-cell-group>
          <div class="submit-area">
            <van-button type="primary" block round native-type="submit" :loading="submitting">
              提交
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>
    <van-popup v-model:show="showCommunityPicker" round position="bottom">
      <van-picker
        :columns="communityOptions.map((item) => ({ text: item.name, value: item.id }))"
        @confirm="onCommunityConfirm"
        @cancel="showCommunityPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import type { TagType } from 'vant'
import type { UploaderFileListItem } from 'vant'
import { listMyWorkOrders, createWorkOrder } from '@/api/workorder'
import type { WorkOrderSimpleVO } from '@/api/workorder'
import { uploadFile, validateFile } from '@/api/common/upload'
import { getCommunities } from '@/api/base'
import type { CascadeOption } from '@/api/base'

const router = useRouter()

const statusMap: Record<string, string> = {
  PENDING_DISPATCH: '待派发',
  PENDING_ACCEPT: '待接单',
  IN_PROGRESS: '处理中',
  PENDING_VERIFY: '待验收',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  FORCE_CLOSED: '已关闭'
}

const orderTypeMap: Record<string, string> = {
  WATER_ELECTRIC: '水电维修',
  DOOR_WINDOW: '门窗维修',
  PIPELINE: '管道疏通',
  PUBLIC_FACILITY: '公共设施',
  OTHER: '其他'
}

const activeTab = ref('')
const orders = ref<WorkOrderSimpleVO[]>([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)
const pageSize = 10
/** 标记是否正在执行 reset 加载，防止 van-list 自动触发 loadMore 时并发追加 */
const resetting = ref(false)

const showCreate = ref(false)
const submitting = ref(false)
const showCommunityPicker = ref(false)
const communityOptions = ref<CascadeOption[]>([])
const createForm = reactive({
  orderType: '',
  addressType: 'HOUSE',
  description: '',
  communityId: undefined as number | undefined
})
const uploadFileList = ref<UploaderFileListItem[]>([])
const uploadedUrls = ref<string[]>([])

function statusTagType(status: string): TagType | undefined {
  const map: Record<string, TagType> = {
    PENDING_DISPATCH: 'warning',
    PENDING_ACCEPT: 'warning',
    IN_PROGRESS: 'primary',
    PENDING_VERIFY: 'success',
    COMPLETED: 'success',
    FORCE_CLOSED: 'danger'
  }
  return map[status]
}

async function fetchOrders(reset = false) {
  if (reset) {
    resetting.value = true
    pageNum.value = 1
    orders.value = []
    finished.value = false
  }
  try {
    const res: any = await listMyWorkOrders({
      status: activeTab.value || undefined,
      pageNum: pageNum.value,
      pageSize
    })
    const list = Array.isArray(res) ? res : (res.data || res.list || [])
    if (reset) {
      orders.value = list
    } else {
      orders.value.push(...list)
    }
    if (list.length < pageSize) {
      finished.value = true
    }
    pageNum.value++
  } catch {
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
    resetting.value = false
  }
}

function loadMore() {
  // reset 进行中时跳过，避免 van-list 自动触发导致数据重复
  if (resetting.value) {
    loading.value = false
    return
  }
  fetchOrders()
}

function onTabChange() {
  fetchOrders(true)
}

function onRefresh() {
  fetchOrders(true)
}

function goDetail(id: number) {
  router.push(`/workorder/${id}`)
}

async function submitCreate() {
  if (!createForm.communityId) {
    showToast('请选择所属小区')
    return
  }
  submitting.value = true
  try {
    await createWorkOrder({
      orderType: createForm.orderType,
      description: createForm.description,
      addressType: createForm.addressType,
      communityId: createForm.communityId,
      imageUrls: uploadedUrls.value.length > 0 ? uploadedUrls.value : undefined
    })
    showToast('提交成功')
    showCreate.value = false
    createForm.orderType = ''
    createForm.description = ''
    createForm.communityId = communityOptions.value[0]?.id
    uploadFileList.value = []
    uploadedUrls.value = []
    fetchOrders(true)
  } finally {
    submitting.value = false
  }
}

async function onAfterRead(item: UploaderFileListItem | UploaderFileListItem[]) {
  const items = Array.isArray(item) ? item : [item]
  for (const file of items) {
    if (!file.file) continue
    const error = validateFile(file.file)
    if (error) {
      showToast(error)
      const idx = uploadFileList.value.indexOf(file)
      if (idx > -1) uploadFileList.value.splice(idx, 1)
      continue
    }
    file.status = 'uploading'
    file.message = '上传中'
    try {
      const url = await uploadFile(file.file!)
      file.status = 'done'
      file.message = ''
      uploadedUrls.value.push(url)
    } catch {
      file.status = 'failed'
      file.message = '上传失败'
    }
  }
}

function onDeleteImage(item: UploaderFileListItem, detail: { index: number }) {
  if (uploadedUrls.value.length > detail.index) {
    uploadedUrls.value.splice(detail.index, 1)
  }
}

function onCommunityConfirm(payload: { selectedValues: Array<string | number> }) {
  const value = payload.selectedValues[0]
  createForm.communityId = value !== undefined ? Number(value) : undefined
  showCommunityPicker.value = false
}

async function loadCommunities() {
  try {
    communityOptions.value = await getCommunities()
    if (!createForm.communityId) {
      createForm.communityId = communityOptions.value[0]?.id
    }
  } catch {
    communityOptions.value = []
  }
}

onMounted(() => {
  fetchOrders(true)
  loadCommunities()
})
</script>

<style scoped>
.workorder-page {
  padding-bottom: 60px;
}
.order-card {
  margin: 8px 12px;
  border-radius: 8px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.order-no {
  font-weight: 600;
  font-size: 14px;
}
.card-desc {
  color: #666;
  font-size: 13px;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-info {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
.card-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
.create-btn {
  position: fixed;
  right: 16px;
  bottom: 80px;
  z-index: 100;
}
.create-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  font-size: 16px;
  font-weight: 600;
}
.create-form {
  flex: 1;
  overflow-y: auto;
  padding: 0 0 16px;
}
.submit-area {
  padding: 16px;
}
</style>
