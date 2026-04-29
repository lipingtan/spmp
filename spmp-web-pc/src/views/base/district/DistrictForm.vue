<template>
  <el-dialog v-model="visible" :title="isEdit ? '编辑片区' : '新增片区'" width="500px" destroy-on-close>
    <el-form ref="formElRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="片区名称" prop="districtName">
        <el-input v-model="form.districtName" placeholder="请输入片区名称" maxlength="64" />
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
import { createDistrict, updateDistrict } from '@/api/base/district'

const emit = defineEmits<{ success: [] }>()

const visible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const editId = ref<number>(0)
const formElRef = ref<FormInstance>()

const form = reactive({
  districtName: '',
  remark: ''
})

const rules: FormRules = {
  districtName: [{ required: true, message: '请输入片区名称', trigger: 'blur' }]
}

async function open(row?: any) {
  visible.value = true
  isEdit.value = !!row
  await nextTick()
  formElRef.value?.resetFields()
  if (row) {
    editId.value = row.id
    form.districtName = row.districtName
    form.remark = row.remark || ''
  } else {
    editId.value = 0
    form.districtName = ''
    form.remark = ''
  }
}

async function handleSubmit() {
  await formElRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateDistrict(editId.value, { districtName: form.districtName, remark: form.remark })
    } else {
      await createDistrict({ districtName: form.districtName, remark: form.remark })
    }
    ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
    visible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>
