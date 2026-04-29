<template>
  <el-dialog v-model="visible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px" destroy-on-close>
    <el-form ref="formElRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="角色名称" prop="roleName">
        <el-input v-model="form.roleName" placeholder="请输入角色名称" />
      </el-form-item>
      <el-form-item label="角色编码" prop="roleCode">
        <el-input v-model="form.roleCode" :disabled="isEdit" placeholder="请输入角色编码" />
      </el-form-item>
      <el-form-item label="数据权限" prop="dataPermissionLevel">
        <el-select v-model="form.dataPermissionLevel" placeholder="请选择">
          <el-option label="全部" value="ALL" />
          <el-option label="片区" value="AREA" />
          <el-option label="小区" value="COMMUNITY" />
          <el-option label="楼栋" value="BUILDING" />
          <el-option label="仅本人" value="SELF" />
        </el-select>
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="form.sort" :min="0" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" />
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
import { createRole, updateRole } from '@/api/role'

const emit = defineEmits<{ success: [] }>()
const visible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const editId = ref(0)
const formElRef = ref<FormInstance>()

const form = reactive({ roleName: '', roleCode: '', dataPermissionLevel: 'SELF', sort: 0, remark: '' })
const rules: FormRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  dataPermissionLevel: [{ required: true, message: '请选择数据权限', trigger: 'change' }]
}

async function open(row?: any) {
  visible.value = true
  isEdit.value = !!row
  await nextTick()
  formElRef.value?.resetFields()
  if (row) {
    editId.value = row.id
    form.roleName = row.roleName
    form.roleCode = row.roleCode
    form.dataPermissionLevel = row.dataPermissionLevel
    form.sort = row.sort
    form.remark = row.remark || ''
  }
}

async function handleSubmit() {
  await formElRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateRole(editId.value, { roleName: form.roleName, dataPermissionLevel: form.dataPermissionLevel, sort: form.sort, remark: form.remark })
    } else {
      await createRole(form)
    }
    ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
    visible.value = false
    emit('success')
  } finally { submitting.value = false }
}

defineExpose({ open })
</script>
