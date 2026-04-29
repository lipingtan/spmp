<template>
  <!-- 房产绑定弹窗 -->
  <el-dialog v-model="visible" title="绑定房产" width="500px" destroy-on-close>
    <el-form ref="formElRef" :model="form" :rules="rules" label-width="100px">
      <!-- 小区选择 -->
      <el-form-item label="小区" prop="communityId">
        <el-select
          v-model="form.communityId"
          placeholder="请选择小区"
          style="width: 100%"
          filterable
          @change="handleCommunityChange"
        >
          <el-option
            v-for="item in communityList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>

      <!-- 楼栋选择 -->
      <el-form-item label="楼栋" prop="buildingId">
        <el-select
          v-model="form.buildingId"
          placeholder="请先选择小区"
          style="width: 100%"
          filterable
          :disabled="!form.communityId"
          @change="handleBuildingChange"
        >
          <el-option
            v-for="item in buildingList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>

      <!-- 单元选择 -->
      <el-form-item label="单元" prop="unitId">
        <el-select
          v-model="form.unitId"
          placeholder="请先选择楼栋"
          style="width: 100%"
          filterable
          :disabled="!form.buildingId"
          @change="handleUnitChange"
        >
          <el-option
            v-for="item in unitList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>

      <!-- 房屋选择 -->
      <el-form-item label="房屋" prop="houseId">
        <el-select
          v-model="form.houseId"
          placeholder="请先选择单元"
          style="width: 100%"
          filterable
          :disabled="!form.unitId"
        >
          <el-option
            v-for="item in houseList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>

      <!-- 关系类型选择 -->
      <el-form-item label="关系类型" prop="relationType">
        <el-select v-model="form.relationType" placeholder="请选择关系类型" style="width: 100%">
          <el-option label="业主" value="OWNER" />
          <el-option label="租户" value="TENANT" />
          <el-option label="家属" value="FAMILY" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
/**
 * 房产绑定弹窗组件
 * 功能：级联选择小区→楼栋→单元→房屋，选择关系类型后提交绑定
 */
import { ref, reactive, nextTick } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { bindProperty } from '@/api/owner/property-binding'
import { getCascadeData, type CascadeItem } from '@/api/base/cascade'

const props = defineProps<{
  /** 业主 ID */
  ownerId: number
}>()

const emit = defineEmits<{ success: [] }>()

/** 弹窗可见状态 */
const visible = ref(false)
/** 提交中状态 */
const submitting = ref(false)
/** 表单引用 */
const formElRef = ref<FormInstance>()

/** 级联数据列表 */
const communityList = ref<CascadeItem[]>([])
const buildingList = ref<CascadeItem[]>([])
const unitList = ref<CascadeItem[]>([])
const houseList = ref<CascadeItem[]>([])

/** 表单数据 */
const form = reactive({
  communityId: undefined as number | undefined,
  buildingId: undefined as number | undefined,
  unitId: undefined as number | undefined,
  houseId: undefined as number | undefined,
  relationType: ''
})

/** 表单校验规则 */
const rules: FormRules = {
  communityId: [{ required: true, message: '请选择小区', trigger: 'change' }],
  buildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  unitId: [{ required: true, message: '请选择单元', trigger: 'change' }],
  houseId: [{ required: true, message: '请选择房屋', trigger: 'change' }],
  relationType: [{ required: true, message: '请选择关系类型', trigger: 'change' }]
}

/** 重置表单 */
function resetForm() {
  form.communityId = undefined
  form.buildingId = undefined
  form.unitId = undefined
  form.houseId = undefined
  form.relationType = ''
  buildingList.value = []
  unitList.value = []
  houseList.value = []
}

/** 小区变更：清空下级并加载楼栋列表 */
async function handleCommunityChange(val: number) {
  form.buildingId = undefined
  form.unitId = undefined
  form.houseId = undefined
  buildingList.value = []
  unitList.value = []
  houseList.value = []
  if (val) {
    try {
      buildingList.value = await getCascadeData('BUILDING', val)
    } catch {
      ElMessage.error('加载楼栋列表失败')
    }
  }
}

/** 楼栋变更：清空下级并加载单元列表 */
async function handleBuildingChange(val: number) {
  form.unitId = undefined
  form.houseId = undefined
  unitList.value = []
  houseList.value = []
  if (val) {
    try {
      unitList.value = await getCascadeData('UNIT', val)
    } catch {
      ElMessage.error('加载单元列表失败')
    }
  }
}

/** 单元变更：清空下级并加载房屋列表 */
async function handleUnitChange(val: number) {
  form.houseId = undefined
  houseList.value = []
  if (val) {
    try {
      houseList.value = await getCascadeData('HOUSE', val)
    } catch {
      ElMessage.error('加载房屋列表失败')
    }
  }
}

/**
 * 打开弹窗，加载小区列表
 */
async function open() {
  visible.value = true
  await nextTick()
  formElRef.value?.resetFields()
  resetForm()
  // 加载小区列表
  try {
    communityList.value = await getCascadeData('COMMUNITY')
  } catch {
    ElMessage.error('加载小区列表失败')
  }
}

/** 提交绑定 */
async function handleSubmit() {
  await formElRef.value?.validate()
  submitting.value = true
  try {
    await bindProperty({
      ownerId: props.ownerId,
      houseId: form.houseId!,
      relationType: form.relationType
    })
    ElMessage.success('绑定房产成功')
    visible.value = false
    emit('success')
  } catch {
    ElMessage.error('绑定房产失败')
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>
