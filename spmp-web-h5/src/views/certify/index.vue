<!--
  H5 房产认证页面
  - 提交认证申请区域：级联选择 小区→楼栋→单元→房屋，提交认证申请
  - 我的认证记录列表：展示认证记录，状态标签颜色区分，驳回显示原因
-->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import type { PickerOption } from 'vant'
import { submitCertification, myCertifications } from '@/api/certification'
import type { CertificationVO } from '@/api/certification'
import { getCommunities, getBuildings, getUnits, getHouses } from '@/api/base'
import type { CascadeOption } from '@/api/base'

// ==================== 级联选择状态 ====================

/** 小区列表 */
const communityList = ref<CascadeOption[]>([])
/** 楼栋列表 */
const buildingList = ref<CascadeOption[]>([])
/** 单元列表 */
const unitList = ref<CascadeOption[]>([])
/** 房屋列表 */
const houseList = ref<CascadeOption[]>([])

/** 已选中的 ID */
const selectedCommunityId = ref<number | null>(null)
const selectedBuildingId = ref<number | null>(null)
const selectedUnitId = ref<number | null>(null)
const selectedHouseId = ref<number | null>(null)

/** 已选中的名称（用于表单展示） */
const selectedCommunityName = ref('')
const selectedBuildingName = ref('')
const selectedUnitName = ref('')
const selectedHouseName = ref('')

/** 当前激活的 Picker 类型 */
type PickerType = 'community' | 'building' | 'unit' | 'house'
const showPicker = ref(false)
const currentPickerType = ref<PickerType>('community')
const pickerTitle = ref('')
const pickerColumns = ref<PickerOption[]>([])

/** 提交加载状态 */
const submitLoading = ref(false)

// ==================== 认证记录状态 ====================

/** 认证记录列表 */
const certList = ref<CertificationVO[]>([])
/** 列表加载状态 */
const listLoading = ref(false)

// ==================== Picker 相关方法 ====================

/** 打开级联选择器 */
function openPicker(type: PickerType) {
  currentPickerType.value = type
  switch (type) {
    case 'community':
      pickerTitle.value = '选择小区'
      pickerColumns.value = communityList.value.map(toPickerOption)
      break
    case 'building':
      if (!selectedCommunityId.value) {
        showToast('请先选择小区')
        return
      }
      pickerTitle.value = '选择楼栋'
      pickerColumns.value = buildingList.value.map(toPickerOption)
      break
    case 'unit':
      if (!selectedBuildingId.value) {
        showToast('请先选择楼栋')
        return
      }
      pickerTitle.value = '选择单元'
      pickerColumns.value = unitList.value.map(toPickerOption)
      break
    case 'house':
      if (!selectedUnitId.value) {
        showToast('请先选择单元')
        return
      }
      pickerTitle.value = '选择房屋'
      pickerColumns.value = houseList.value.map(toPickerOption)
      break
  }
  showPicker.value = true
}

/** 将 CascadeOption 转为 Vant PickerOption */
function toPickerOption(item: CascadeOption): PickerOption {
  return { text: item.name || String(item.id), value: item.id }
}

/** Picker 确认选择 */
async function onPickerConfirm({ selectedOptions }: { selectedOptions: PickerOption[] }) {
  const selected = selectedOptions[0]
  if (!selected) return
  showPicker.value = false

  const id = selected.value as number
  const name = selected.text as string

  switch (currentPickerType.value) {
    case 'community':
      selectedCommunityId.value = id
      selectedCommunityName.value = name
      // 清空下级选择
      resetFromBuilding()
      // 加载楼栋
      await loadBuildings(id)
      break
    case 'building':
      selectedBuildingId.value = id
      selectedBuildingName.value = name
      // 清空下级选择
      resetFromUnit()
      // 加载单元
      await loadUnits(id)
      break
    case 'unit':
      selectedUnitId.value = id
      selectedUnitName.value = name
      // 清空下级选择
      resetFromHouse()
      // 加载房屋
      await loadHouses(id)
      break
    case 'house':
      selectedHouseId.value = id
      selectedHouseName.value = name
      break
  }
}

/** Picker 取消 */
function onPickerCancel() {
  showPicker.value = false
}

// ==================== 级联数据加载 ====================

/** 加载小区列表 */
async function loadCommunities() {
  try {
    communityList.value = await getCommunities()
  } catch {
    // 错误已在拦截器中处理
  }
}

/** 加载楼栋列表 */
async function loadBuildings(communityId: number) {
  try {
    buildingList.value = await getBuildings(communityId)
  } catch {
    // 错误已在拦截器中处理
  }
}

/** 加载单元列表 */
async function loadUnits(buildingId: number) {
  try {
    unitList.value = await getUnits(buildingId)
  } catch {
    // 错误已在拦截器中处理
  }
}

/** 加载房屋列表 */
async function loadHouses(unitId: number) {
  try {
    houseList.value = await getHouses(unitId)
  } catch {
    // 错误已在拦截器中处理
  }
}

// ==================== 重置方法 ====================

/** 从楼栋开始重置 */
function resetFromBuilding() {
  selectedBuildingId.value = null
  selectedBuildingName.value = ''
  buildingList.value = []
  resetFromUnit()
}

/** 从单元开始重置 */
function resetFromUnit() {
  selectedUnitId.value = null
  selectedUnitName.value = ''
  unitList.value = []
  resetFromHouse()
}

/** 从房屋开始重置 */
function resetFromHouse() {
  selectedHouseId.value = null
  selectedHouseName.value = ''
  houseList.value = []
}

// ==================== 提交认证申请 ====================

/** 提交认证申请 */
async function handleSubmit() {
  if (!selectedCommunityId.value) {
    showToast('请选择小区')
    return
  }
  if (!selectedBuildingId.value) {
    showToast('请选择楼栋')
    return
  }
  if (!selectedUnitId.value) {
    showToast('请选择单元')
    return
  }
  if (!selectedHouseId.value) {
    showToast('请选择房屋')
    return
  }

  submitLoading.value = true
  try {
    await submitCertification({
      communityId: selectedCommunityId.value,
      buildingId: selectedBuildingId.value,
      unitId: selectedUnitId.value,
      houseId: selectedHouseId.value
    })
    showToast({ message: '认证申请已提交', type: 'success' })
    // 重置表单
    selectedCommunityId.value = null
    selectedCommunityName.value = ''
    resetFromBuilding()
    // 重新加载小区列表和认证记录
    await loadCommunities()
    await fetchCertList()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    submitLoading.value = false
  }
}

// ==================== 认证记录 ====================

/** 加载认证记录列表 */
async function fetchCertList() {
  listLoading.value = true
  try {
    certList.value = await myCertifications()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    listLoading.value = false
  }
}

/** 认证状态文本映射 */
function statusText(status: string): string {
  const map: Record<string, string> = {
    PENDING: '待审批',
    APPROVED: '已通过',
    REJECTED: '已驳回'
  }
  return map[status] ?? status
}

/** 认证状态标签类型映射 */
function statusTagType(status: string): 'warning' | 'success' | 'danger' {
  const map: Record<string, 'warning' | 'success' | 'danger'> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return map[status] ?? 'warning'
}

/** 拼接房屋完整地址 */
function fullAddress(item: CertificationVO): string {
  return `${item.communityName} ${item.buildingName} ${item.unitName} ${item.houseName}`
}

// ==================== 生命周期 ====================

onMounted(async () => {
  await Promise.all([loadCommunities(), fetchCertList()])
})
</script>

<template>
  <div class="certify-page">
    <!-- 顶部导航栏 -->
    <van-nav-bar title="房产认证" left-arrow @click-left="$router.back()" />

    <!-- 提交认证申请区域 -->
    <div class="section">
      <div class="section-title">提交认证申请</div>
      <van-form @submit="handleSubmit">
        <van-cell-group inset>
          <!-- 小区选择 -->
          <van-field
            v-model="selectedCommunityName"
            label="小区"
            placeholder="请选择小区"
            readonly
            is-link
            required
            @click="openPicker('community')"
          />
          <!-- 楼栋选择 -->
          <van-field
            v-model="selectedBuildingName"
            label="楼栋"
            placeholder="请选择楼栋"
            readonly
            is-link
            required
            :disabled="!selectedCommunityId"
            @click="openPicker('building')"
          />
          <!-- 单元选择 -->
          <van-field
            v-model="selectedUnitName"
            label="单元"
            placeholder="请选择单元"
            readonly
            is-link
            required
            :disabled="!selectedBuildingId"
            @click="openPicker('unit')"
          />
          <!-- 房屋选择 -->
          <van-field
            v-model="selectedHouseName"
            label="房屋"
            placeholder="请选择房屋"
            readonly
            is-link
            required
            :disabled="!selectedUnitId"
            @click="openPicker('house')"
          />
        </van-cell-group>

        <!-- 提交按钮 -->
        <div class="submit-action">
          <van-button
            type="primary"
            block
            round
            native-type="submit"
            :loading="submitLoading"
            loading-text="提交中..."
          >
            提交认证申请
          </van-button>
        </div>
      </van-form>
    </div>

    <!-- 我的认证记录列表 -->
    <div class="section">
      <div class="section-title">我的认证记录</div>
      <div v-if="listLoading" class="list-loading">
        <van-loading size="24px">加载中...</van-loading>
      </div>
      <template v-else>
        <van-empty v-if="!certList.length" description="暂无认证记录" />
        <div v-else class="cert-list">
          <van-cell-group inset v-for="item in certList" :key="item.id" class="cert-item">
            <van-cell :border="false">
              <template #title>
                <div class="cert-address">{{ fullAddress(item) }}</div>
              </template>
              <template #right-icon>
                <van-tag :type="statusTagType(item.status)" size="medium">
                  {{ statusText(item.status) }}
                </van-tag>
              </template>
            </van-cell>
            <van-cell :border="false" class="cert-time">
              <template #title>
                <span class="time-label">申请时间：{{ item.applyTime }}</span>
              </template>
            </van-cell>
            <!-- 驳回时显示驳回原因 -->
            <van-cell
              v-if="item.status === 'REJECTED' && item.rejectReason"
              :border="false"
              class="cert-reject"
            >
              <template #title>
                <span class="reject-label">驳回原因：{{ item.rejectReason }}</span>
              </template>
            </van-cell>
          </van-cell-group>
        </div>
      </template>
    </div>

    <!-- 级联选择 Picker 弹出层 -->
    <van-popup v-model:show="showPicker" position="bottom" round>
      <van-picker
        :title="pickerTitle"
        :columns="pickerColumns"
        @confirm="onPickerConfirm"
        @cancel="onPickerCancel"
      />
    </van-popup>
  </div>
</template>

<style scoped>
.certify-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--color-bg-page);
}

.section {
  margin-top: var(--spacing-lg);
}

.section-title {
  padding: 0 var(--spacing-lg);
  margin-bottom: var(--spacing-sm);
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.submit-action {
  padding: var(--spacing-xl) var(--spacing-lg) 0;
}

.list-loading {
  display: flex;
  justify-content: center;
  padding: var(--spacing-xxl) 0;
}

.cert-list {
  padding-bottom: var(--spacing-lg);
}

.cert-item {
  margin-bottom: var(--spacing-sm);
}

.cert-address {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  line-height: 1.4;
}

.cert-time {
  padding-top: 0;
}

.cert-time .time-label {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.cert-reject {
  padding-top: 0;
}

.cert-reject .reject-label {
  font-size: 12px;
  color: var(--color-error);
}
</style>
