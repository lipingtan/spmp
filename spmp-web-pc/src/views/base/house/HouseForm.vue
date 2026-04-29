<template>
  <el-dialog v-model="visible" :title="isEdit ? '编辑房屋' : '新增房屋'" width="600px" destroy-on-close>
    <el-form ref="formElRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="房屋编号" prop="houseCode">
        <el-input v-model="form.houseCode" placeholder="请输入房屋编号" maxlength="32" />
      </el-form-item>
      <el-form-item label="所属单元" prop="unitId" v-if="!isEdit">
        <!-- 三级联动：小区→楼栋→单元 -->
        <el-row :gutter="8" style="width: 100%">
          <el-col :span="8">
            <el-select v-model="cascadeCommunityId" placeholder="选择小区" clearable filterable @change="onFormCommunityChange" style="width: 100%">
              <el-option v-for="c in formCommunityOptions" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-col>
          <el-col :span="8">
            <el-select v-model="cascadeBuildingId" placeholder="选择楼栋" clearable filterable :disabled="!cascadeCommunityId" @change="onFormBuildingChange" style="width: 100%">
              <el-option v-for="b in formBuildingOptions" :key="b.id" :label="b.name" :value="b.id" />
            </el-select>
          </el-col>
          <el-col :span="8">
            <el-select v-model="form.unitId" placeholder="选择单元" clearable filterable :disabled="!cascadeBuildingId" style="width: 100%">
              <el-option v-for="u in formUnitOptions" :key="u.id" :label="u.name" :value="u.id" />
            </el-select>
          </el-col>
        </el-row>
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="楼层" prop="floor">
            <el-input-number v-model="form.floor" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="建筑面积" prop="buildingArea">
            <el-input-number v-model="form.buildingArea" :min="0" :precision="2" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="使用面积" prop="usableArea">
            <el-input-number v-model="form.usableArea" :min="0" :precision="2" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="房屋状态" prop="houseStatus" v-if="!isEdit">
            <el-select v-model="form.houseStatus" placeholder="请选择" style="width: 100%">
              <el-option label="空置" value="VACANT" />
              <el-option label="已入住" value="OCCUPIED" />
              <el-option label="已出租" value="RENTED" />
              <el-option label="装修中" value="RENOVATING" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="房屋类型" prop="houseType">
            <el-select v-model="form.houseType" placeholder="请选择" style="width: 100%">
              <el-option label="住宅" value="RESIDENCE" />
              <el-option label="商铺" value="SHOP" />
              <el-option label="车位" value="PARKING" />
              <el-option label="办公" value="OFFICE" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" maxlength="256" :rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createHouse, updateHouse } from '@/api/base/house'
import type { CascadeItem } from '@/api/base/cascade'
import { getCascadeData } from '@/api/base/cascade'
import { useBaseStore } from '@/store/modules/base'

const emit = defineEmits<{ success: [] }>()
const baseStore = useBaseStore()

const visible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const editId = ref<number>(0)
const formElRef = ref<FormInstance>()

// 级联选择
const formCommunityOptions = ref<CascadeItem[]>([])
const formBuildingOptions = ref<CascadeItem[]>([])
const formUnitOptions = ref<CascadeItem[]>([])
const cascadeCommunityId = ref<number>()
const cascadeBuildingId = ref<number>()

const form = reactive({
  houseCode: '',
  unitId: undefined as number | undefined,
  floor: 1,
  buildingArea: undefined as number | undefined,
  usableArea: undefined as number | undefined,
  houseStatus: 'VACANT',
  houseType: 'RESIDENCE',
  remark: ''
})

const rules: FormRules = {
  houseCode: [{ required: true, message: '请输入房屋编号', trigger: 'blur' }],
  unitId: [{ required: true, message: '请选择所属单元', trigger: 'change' }],
  floor: [{ required: true, message: '请输入楼层', trigger: 'blur' }],
  buildingArea: [{ required: true, message: '请输入建筑面积', trigger: 'blur' }],
  houseStatus: [{ required: true, message: '请选择房屋状态', trigger: 'change' }],
  houseType: [{ required: true, message: '请选择房屋类型', trigger: 'change' }]
}

async function onFormCommunityChange(val: number | undefined) {
  cascadeBuildingId.value = undefined
  form.unitId = undefined
  formBuildingOptions.value = []
  formUnitOptions.value = []
  if (val) formBuildingOptions.value = await baseStore.loadBuildings(val)
}

async function onFormBuildingChange(val: number | undefined) {
  form.unitId = undefined
  formUnitOptions.value = []
  if (val) formUnitOptions.value = await baseStore.loadUnits(val)
}

async function open(row?: any) {
  visible.value = true
  isEdit.value = !!row
  // 加载小区列表
  formCommunityOptions.value = await getCascadeData('COMMUNITY')
  await nextTick()
  formElRef.value?.resetFields()
  cascadeCommunityId.value = undefined
  cascadeBuildingId.value = undefined
  formBuildingOptions.value = []
  formUnitOptions.value = []
  if (row) {
    editId.value = row.id
    Object.assign(form, {
      houseCode: row.houseCode, unitId: row.unitId, floor: row.floor,
      buildingArea: row.buildingArea, usableArea: row.usableArea,
      houseStatus: row.houseStatus, houseType: row.houseType, remark: row.remark || ''
    })
  } else {
    editId.value = 0
    Object.assign(form, { houseCode: '', unitId: undefined, floor: 1, buildingArea: undefined, usableArea: undefined, houseStatus: 'VACANT', houseType: 'RESIDENCE', remark: '' })
  }
}

async function handleSubmit() {
  await formElRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateHouse(editId.value, {
        houseCode: form.houseCode, floor: form.floor, buildingArea: form.buildingArea!,
        usableArea: form.usableArea, houseType: form.houseType, remark: form.remark
      })
    } else {
      await createHouse({
        houseCode: form.houseCode, unitId: form.unitId!, floor: form.floor,
        buildingArea: form.buildingArea!, usableArea: form.usableArea,
        houseStatus: form.houseStatus, houseType: form.houseType, remark: form.remark
      })
    }
    ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
    visible.value = false
    baseStore.clearCache()
    emit('success')
  } finally { submitting.value = false }
}

defineExpose({ open })
</script>
