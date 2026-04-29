<template>
  <el-dialog v-model="visible" :title="isEdit ? '编辑楼栋' : '新增楼栋'" width="600px" destroy-on-close>
    <el-form ref="formElRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="楼栋名称" prop="buildingName">
        <el-input v-model="form.buildingName" placeholder="请输入楼栋名称" maxlength="64" />
      </el-form-item>
      <el-form-item label="楼栋编号" prop="buildingCode">
        <el-input v-model="form.buildingCode" placeholder="请输入楼栋编号" maxlength="32" />
      </el-form-item>
      <el-form-item label="所属小区" prop="communityId">
        <el-select v-model="form.communityId" placeholder="请选择所属小区" style="width: 100%" :disabled="isEdit" filterable>
          <el-option v-for="c in communityOptions" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="地上层数" prop="aboveGroundFloors">
            <el-input-number v-model="form.aboveGroundFloors" :min="0" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="地下层数" prop="undergroundFloors">
            <el-input-number v-model="form.undergroundFloors" :min="0" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="每层户数" prop="unitsPerFloor">
            <el-input-number v-model="form.unitsPerFloor" :min="0" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="楼栋类型" prop="buildingType">
        <el-select v-model="form.buildingType" placeholder="请选择楼栋类型" style="width: 100%">
          <el-option label="住宅" value="RESIDENTIAL" />
          <el-option label="商业" value="COMMERCIAL" />
          <el-option label="车库" value="GARAGE" />
          <el-option label="混合" value="MIXED" />
          <el-option label="其他" value="OTHER" />
        </el-select>
      </el-form-item>
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
import { ref, reactive, nextTick } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createBuilding, updateBuilding } from '@/api/base/building'
import type { CascadeItem } from '@/api/base/cascade'
import { useBaseStore } from '@/store/modules/base'

defineProps<{ communityOptions: CascadeItem[] }>()
const emit = defineEmits<{ success: [] }>()
const baseStore = useBaseStore()

const visible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const editId = ref<number>(0)
const formElRef = ref<FormInstance>()

const form = reactive({
  buildingName: '',
  buildingCode: '',
  communityId: undefined as number | undefined,
  aboveGroundFloors: 0,
  undergroundFloors: 0,
  unitsPerFloor: 0,
  buildingType: 'RESIDENTIAL',
  remark: ''
})

const rules: FormRules = {
  buildingName: [{ required: true, message: '请输入楼栋名称', trigger: 'blur' }],
  buildingCode: [{ required: true, message: '请输入楼栋编号', trigger: 'blur' }],
  communityId: [{ required: true, message: '请选择所属小区', trigger: 'change' }],
  aboveGroundFloors: [{ required: true, message: '请输入地上层数', trigger: 'blur' }],
  undergroundFloors: [{ required: true, message: '请输入地下层数', trigger: 'blur' }],
  unitsPerFloor: [{ required: true, message: '请输入每层户数', trigger: 'blur' }],
  buildingType: [{ required: true, message: '请选择楼栋类型', trigger: 'change' }]
}

async function open(row?: any) {
  visible.value = true
  isEdit.value = !!row
  await nextTick()
  formElRef.value?.resetFields()
  if (row) {
    editId.value = row.id
    Object.assign(form, {
      buildingName: row.buildingName, buildingCode: row.buildingCode, communityId: row.communityId,
      aboveGroundFloors: row.aboveGroundFloors, undergroundFloors: row.undergroundFloors,
      unitsPerFloor: row.unitsPerFloor, buildingType: row.buildingType, remark: row.remark || ''
    })
  } else {
    editId.value = 0
    Object.assign(form, { buildingName: '', buildingCode: '', communityId: undefined, aboveGroundFloors: 0, undergroundFloors: 0, unitsPerFloor: 0, buildingType: 'RESIDENTIAL', remark: '' })
  }
}

async function handleSubmit() {
  await formElRef.value?.validate()
  submitting.value = true
  try {
    const { buildingName, buildingCode, aboveGroundFloors, undergroundFloors, unitsPerFloor, buildingType, remark } = form
    if (isEdit.value) {
      await updateBuilding(editId.value, { buildingName, buildingCode, aboveGroundFloors, undergroundFloors, unitsPerFloor, buildingType, remark })
    } else {
      await createBuilding({ buildingName, buildingCode, communityId: form.communityId!, aboveGroundFloors, undergroundFloors, unitsPerFloor, buildingType, remark })
    }
    ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
    visible.value = false
    baseStore.clearCache()
    emit('success')
  } finally { submitting.value = false }
}

defineExpose({ open })
</script>
