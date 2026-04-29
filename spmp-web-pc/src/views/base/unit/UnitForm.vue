<template>
  <el-dialog v-model="visible" :title="isEdit ? '编辑单元' : '新增单元'" width="500px" destroy-on-close>
    <el-form ref="formElRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="单元名称" prop="unitName">
        <el-input v-model="form.unitName" placeholder="请输入单元名称" maxlength="64" />
      </el-form-item>
      <el-form-item label="单元编号" prop="unitCode">
        <el-input v-model="form.unitCode" placeholder="请输入单元编号" maxlength="32" />
      </el-form-item>
      <el-form-item label="所属楼栋" prop="buildingId">
        <el-select v-model="form.buildingId" placeholder="请选择所属楼栋" style="width: 100%" :disabled="isEdit" filterable>
          <el-option v-for="b in mergedBuildingOptions" :key="b.id" :label="b.name" :value="b.id" />
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
import { ref, reactive, nextTick, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createUnit, updateUnit } from '@/api/base/unit'
import type { CascadeItem } from '@/api/base/cascade'
import { useBaseStore } from '@/store/modules/base'

const props = defineProps<{ buildingOptions: CascadeItem[] }>()
const emit = defineEmits<{ success: [] }>()
const baseStore = useBaseStore()

const visible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const editId = ref<number>(0)
const formElRef = ref<FormInstance>()
const editBuildingName = ref('')

const form = reactive({
  unitName: '',
  unitCode: '',
  buildingId: undefined as number | undefined,
  remark: ''
})

const rules: FormRules = {
  unitName: [{ required: true, message: '请输入单元名称', trigger: 'blur' }],
  unitCode: [{ required: true, message: '请输入单元编号', trigger: 'blur' }],
  buildingId: [{ required: true, message: '请选择所属楼栋', trigger: 'change' }]
}

const mergedBuildingOptions = computed(() => {
  if (!isEdit.value || !form.buildingId || !editBuildingName.value) {
    return props.buildingOptions
  }
  const exists = props.buildingOptions.some((item) => item.id === form.buildingId)
  if (exists) {
    return props.buildingOptions
  }
  return [{ id: form.buildingId, name: editBuildingName.value, code: '', status: 0 }, ...props.buildingOptions]
})

async function open(row?: any) {
  visible.value = true
  isEdit.value = !!row
  await nextTick()
  formElRef.value?.resetFields()
  if (row) {
    editId.value = row.id
    editBuildingName.value = row.buildingName || ''
    Object.assign(form, { unitName: row.unitName, unitCode: row.unitCode, buildingId: row.buildingId, remark: row.remark || '' })
  } else {
    editId.value = 0
    editBuildingName.value = ''
    Object.assign(form, { unitName: '', unitCode: '', buildingId: undefined, remark: '' })
  }
}

async function handleSubmit() {
  await formElRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateUnit(editId.value, { unitName: form.unitName, unitCode: form.unitCode, remark: form.remark })
    } else {
      await createUnit({ unitName: form.unitName, unitCode: form.unitCode, buildingId: form.buildingId!, remark: form.remark })
    }
    ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
    visible.value = false
    baseStore.clearCache()
    emit('success')
  } finally { submitting.value = false }
}

defineExpose({ open })
</script>
