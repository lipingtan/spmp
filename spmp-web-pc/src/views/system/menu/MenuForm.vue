<template>
  <el-dialog v-model="visible" :title="isEdit ? '编辑菜单' : '新增菜单'" width="550px" destroy-on-close>
    <el-form ref="formElRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="菜单类型" prop="menuType" v-if="!isEdit">
        <el-radio-group v-model="form.menuType">
          <el-radio value="D">目录</el-radio>
          <el-radio value="M">菜单</el-radio>
          <el-radio value="B">按钮</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="菜单名称" prop="menuName">
        <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
      </el-form-item>
      <el-form-item label="父级ID" prop="parentId" v-if="!isEdit">
        <el-input-number v-model="form.parentId" :min="0" />
      </el-form-item>
      <el-form-item label="路由路径" v-if="form.menuType !== 'B'">
        <el-input v-model="form.path" placeholder="请输入路由路径" />
      </el-form-item>
      <el-form-item label="组件路径" v-if="form.menuType === 'M'">
        <el-input v-model="form.component" placeholder="请输入组件路径" />
      </el-form-item>
      <el-form-item label="权限标识" v-if="form.menuType !== 'D'">
        <el-input v-model="form.permission" placeholder="如 user:user:list" />
      </el-form-item>
      <el-form-item label="图标" v-if="form.menuType !== 'B'">
        <el-input v-model="form.icon" placeholder="请输入图标名称" />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="form.sort" :min="0" />
      </el-form-item>
      <el-form-item label="状态" v-if="isEdit">
        <el-select v-model="form.status">
          <el-option label="启用" :value="0" />
          <el-option label="禁用" :value="1" />
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
import { ref, reactive, nextTick } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createMenu, updateMenu } from '@/api/menu'

const emit = defineEmits<{ success: [] }>()
const visible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const editId = ref(0)
const formElRef = ref<FormInstance>()

const form = reactive({ menuName: '', parentId: 0, menuType: 'M', path: '', component: '', permission: '', icon: '', sort: 0, status: 0 })
const rules: FormRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

async function open(row?: any, parentId?: number) {
  visible.value = true
  isEdit.value = !!row
  await nextTick()
  formElRef.value?.resetFields()
  if (row) {
    editId.value = row.id
    form.menuName = row.menuName
    form.menuType = row.menuType
    form.path = row.path || ''
    form.component = row.component || ''
    form.permission = row.permission || ''
    form.icon = row.icon || ''
    form.sort = row.sort
    form.status = row.status
    form.parentId = row.parentId
  } else {
    form.parentId = parentId || 0
  }
}

async function handleSubmit() {
  await formElRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateMenu(editId.value, { menuName: form.menuName, path: form.path, component: form.component, permission: form.permission, icon: form.icon, sort: form.sort, status: form.status })
    } else {
      await createMenu(form)
    }
    ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
    visible.value = false
    emit('success')
  } finally { submitting.value = false }
}

defineExpose({ open })
</script>
