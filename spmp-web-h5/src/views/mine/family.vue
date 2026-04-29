<!--
  H5 家庭成员页面
  - 家庭成员列表：van-cell-group + van-cell 展示姓名、手机号(脱敏)、与业主关系(van-tag)
  - 添加家庭成员：底部固定按钮，弹出 van-popup 表单（姓名、手机号、身份证号、关系）
  - 删除家庭成员：van-swipe-cell 右滑删除，van-dialog 确认
  - 空状态使用 van-empty
-->
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import type { FormInstance, PickerOption } from 'vant'
import { listFamilyMembers, addFamilyMember, deleteFamilyMember } from '@/api/family'
import type { FamilyMemberVO, FamilyMemberCreateDTO } from '@/api/family'

// ==================== 列表状态 ====================

/** 家庭成员列表 */
const memberList = ref<FamilyMemberVO[]>([])
/** 列表加载状态 */
const loading = ref(false)

// ==================== 添加弹窗状态 ====================

/** 弹窗显示状态 */
const showAddPopup = ref(false)
/** 表单引用 */
const addFormRef = ref<FormInstance>()
/** 表单数据 */
const addForm = reactive<FamilyMemberCreateDTO>({
  name: '',
  phone: '',
  idCard: '',
  relation: ''
})
/** 提交加载状态 */
const submitLoading = ref(false)

// ==================== 关系选择器状态 ====================

/** 关系选择器显示状态 */
const showRelationPicker = ref(false)
/** 关系选择器选项 */
const relationColumns: PickerOption[] = [
  { text: '配偶', value: 'SPOUSE' },
  { text: '父母', value: 'PARENT' },
  { text: '子女', value: 'CHILD' },
  { text: '其他', value: 'OTHER' }
]
/** 关系显示文本 */
const relationText = ref('')

// ==================== 表单校验规则 ====================

/** 手机号正则 */
const phonePattern = /^1[3-9]\d{9}$/

const nameRules = [{ required: true, message: '请输入姓名' }]
const phoneRules = [
  { required: true, message: '请输入手机号' },
  { pattern: phonePattern, message: '手机号格式不正确' }
]
const idCardRules = [{ required: true, message: '请输入身份证号' }]
const relationRules = [{ required: true, message: '请选择与业主关系' }]

// ==================== 关系映射 ====================

/** 关系枚举 → 中文文本 */
function relationLabel(relation: string): string {
  const map: Record<string, string> = {
    SPOUSE: '配偶',
    PARENT: '父母',
    CHILD: '子女',
    OTHER: '其他'
  }
  return map[relation] ?? relation
}

/** 关系枚举 → 标签颜色 */
function relationTagType(relation: string): 'primary' | 'success' | 'warning' | 'danger' {
  const map: Record<string, 'primary' | 'success' | 'warning' | 'danger'> = {
    SPOUSE: 'primary',
    PARENT: 'success',
    CHILD: 'warning',
    OTHER: 'danger'
  }
  return map[relation] ?? 'primary'
}

// ==================== 数据加载 ====================

/** 加载家庭成员列表 */
async function fetchMembers() {
  loading.value = true
  try {
    memberList.value = await listFamilyMembers()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// ==================== 添加家庭成员 ====================

/** 打开添加弹窗 */
function openAddPopup() {
  // 重置表单
  addForm.name = ''
  addForm.phone = ''
  addForm.idCard = ''
  addForm.relation = ''
  relationText.value = ''
  showAddPopup.value = true
}

/** 关系选择器确认 */
function onRelationConfirm({ selectedOptions }: { selectedOptions: PickerOption[] }) {
  const selected = selectedOptions[0]
  if (selected) {
    addForm.relation = selected.value as string
    relationText.value = selected.text as string
  }
  showRelationPicker.value = false
}

/** 提交添加家庭成员 */
async function handleAdd() {
  try {
    await addFormRef.value?.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    await addFamilyMember(addForm)
    showToast({ message: '添加成功', type: 'success' })
    showAddPopup.value = false
    await fetchMembers()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    submitLoading.value = false
  }
}

// ==================== 删除家庭成员 ====================

/** 确认删除家庭成员 */
async function handleDelete(member: FamilyMemberVO) {
  try {
    await showConfirmDialog({
      title: '删除确认',
      message: `确定要删除家庭成员「${member.name}」吗？`
    })
  } catch {
    // 用户取消
    return
  }

  try {
    await deleteFamilyMember(member.id)
    showToast({ message: '删除成功', type: 'success' })
    await fetchMembers()
  } catch {
    // 错误已在拦截器中处理
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchMembers()
})
</script>

<template>
  <div class="family-page">
    <!-- 顶部导航栏 -->
    <van-nav-bar title="家庭成员" left-arrow @click-left="$router.back()" />

    <!-- 加载状态 -->
    <div v-if="loading" class="page-loading">
      <van-loading size="24px">加载中...</van-loading>
    </div>

    <!-- 家庭成员列表 -->
    <template v-else>
      <van-empty v-if="!memberList.length" description="暂无家庭成员" />
      <div v-else class="member-list">
        <van-swipe-cell v-for="item in memberList" :key="item.id">
          <van-cell-group inset class="member-item">
            <van-cell :border="false">
              <template #title>
                <div class="member-info">
                  <span class="member-name">{{ item.name }}</span>
                  <van-tag :type="relationTagType(item.relation)" size="medium" class="member-tag">
                    {{ relationLabel(item.relation) }}
                  </van-tag>
                </div>
              </template>
              <template #label>
                <span class="member-phone">{{ item.phoneMasked }}</span>
              </template>
            </van-cell>
          </van-cell-group>
          <template #right>
            <van-button
              square
              type="danger"
              text="删除"
              class="swipe-delete-btn"
              @click="handleDelete(item)"
            />
          </template>
        </van-swipe-cell>
      </div>
    </template>

    <!-- 底部固定添加按钮 -->
    <div class="bottom-action">
      <van-button type="primary" block round @click="openAddPopup">
        添加家庭成员
      </van-button>
    </div>

    <!-- 添加家庭成员弹窗 -->
    <van-popup
      v-model:show="showAddPopup"
      position="bottom"
      round
      :style="{ maxHeight: '80%' }"
    >
      <div class="popup-header">
        <span class="popup-title">添加家庭成员</span>
        <van-icon name="cross" class="popup-close" @click="showAddPopup = false" />
      </div>
      <van-form ref="addFormRef" @submit="handleAdd">
        <van-cell-group inset>
          <!-- 姓名 -->
          <van-field
            v-model="addForm.name"
            name="name"
            label="姓名"
            placeholder="请输入姓名"
            clearable
            required
            :rules="nameRules"
          />
          <!-- 手机号 -->
          <van-field
            v-model="addForm.phone"
            name="phone"
            label="手机号"
            placeholder="请输入手机号"
            type="tel"
            maxlength="11"
            clearable
            required
            :rules="phoneRules"
          />
          <!-- 身份证号 -->
          <van-field
            v-model="addForm.idCard"
            name="idCard"
            label="身份证号"
            placeholder="请输入身份证号"
            maxlength="18"
            clearable
            required
            :rules="idCardRules"
          />
          <!-- 与业主关系 -->
          <van-field
            v-model="relationText"
            name="relation"
            label="与业主关系"
            placeholder="请选择与业主关系"
            readonly
            is-link
            required
            :rules="relationRules"
            @click="showRelationPicker = true"
          />
        </van-cell-group>

        <!-- 提交按钮 -->
        <div class="popup-action">
          <van-button
            type="primary"
            block
            round
            native-type="submit"
            :loading="submitLoading"
            loading-text="提交中..."
          >
            确认添加
          </van-button>
        </div>
      </van-form>
    </van-popup>

    <!-- 关系选择器 -->
    <van-popup v-model:show="showRelationPicker" position="bottom" round>
      <van-picker
        title="选择与业主关系"
        :columns="relationColumns"
        @confirm="onRelationConfirm"
        @cancel="showRelationPicker = false"
      />
    </van-popup>
  </div>
</template>

<style scoped>
.family-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--color-bg-page);
  padding-bottom: 80px;
}

.page-loading {
  display: flex;
  justify-content: center;
  padding: var(--spacing-xxl) 0;
}

.member-list {
  padding: var(--spacing-lg) 0;
}

.member-item {
  margin-bottom: var(--spacing-sm);
}

.member-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.member-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.member-tag {
  flex-shrink: 0;
}

.member-phone {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.swipe-delete-btn {
  height: 100%;
}

.bottom-action {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: var(--spacing-md) var(--spacing-lg);
  background-color: var(--color-bg-page);
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
}

.popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--color-border);
}

.popup-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.popup-close {
  font-size: 18px;
  color: var(--color-text-secondary);
  cursor: pointer;
}

.popup-action {
  padding: var(--spacing-xl) var(--spacing-lg);
}
</style>
