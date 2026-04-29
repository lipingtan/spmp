<template>
  <!-- 业主新增/编辑弹窗 -->
  <el-dialog v-model="visible" :title="isEdit ? '编辑业主' : '新增业主'" width="600px" destroy-on-close>
    <el-form ref="formElRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="form.name" placeholder="请输入姓名" maxlength="64" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
      </el-form-item>
      <el-form-item label="身份证号" prop="idCard">
        <el-input v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" />
      </el-form-item>
      <el-form-item label="性别" prop="gender">
        <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
          <el-option label="未知" :value="0" />
          <el-option label="男" :value="1" />
          <el-option label="女" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="头像" prop="avatar">
        <el-input v-model="form.avatar" placeholder="请输入头像URL" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="紧急联系人" prop="emergencyContact">
        <el-input v-model="form.emergencyContact" placeholder="请输入紧急联系人" />
      </el-form-item>
      <el-form-item label="紧急联系电话" prop="emergencyPhone">
        <el-input v-model="form.emergencyPhone" placeholder="请输入紧急联系电话" />
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
 * 业主新增/编辑弹窗组件
 * 新增模式：表单为空，标题"新增业主"
 * 编辑模式：根据业主 ID 加载详情填充表单，标题"编辑业主"
 */
import { ref, reactive, nextTick } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createOwner, updateOwner, getOwner } from '@/api/owner/owner'
import type { OwnerCreateDTO, OwnerUpdateDTO } from '@/api/owner/owner'

const emit = defineEmits<{ success: [] }>()

/** 弹窗可见状态 */
const visible = ref(false)
/** 是否编辑模式 */
const isEdit = ref(false)
/** 提交中状态 */
const submitting = ref(false)
/** 编辑时的业主 ID */
const editId = ref<number>(0)
/** 表单引用 */
const formElRef = ref<FormInstance>()

/** 表单数据 */
const form = reactive({
  name: '',
  phone: '',
  idCard: '',
  gender: 0,
  avatar: '',
  email: '',
  emergencyContact: '',
  emergencyPhone: ''
})

/** 表单校验规则 */
const rules: FormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{17}(\d|X|x)$)/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

/** 重置表单为初始值 */
function resetForm() {
  form.name = ''
  form.phone = ''
  form.idCard = ''
  form.gender = 0
  form.avatar = ''
  form.email = ''
  form.emergencyContact = ''
  form.emergencyPhone = ''
}

/**
 * 打开弹窗
 * @param id 业主 ID，传入时为编辑模式，不传为新增模式
 */
async function open(id?: number) {
  visible.value = true
  isEdit.value = !!id
  editId.value = id || 0
  await nextTick()
  formElRef.value?.resetFields()
  resetForm()

  if (id) {
    // 编辑模式：加载业主详情填充表单
    try {
      const res: any = await getOwner(id)
      const data = res.data || res
      form.name = data.name || ''
      form.phone = data.phone || ''
      form.idCard = data.idCard || ''
      form.gender = data.gender ?? 0
      form.avatar = data.avatar || ''
      form.email = data.email || ''
      form.emergencyContact = data.emergencyContact || ''
      form.emergencyPhone = data.emergencyPhone || ''
    } catch {
      ElMessage.error('加载业主信息失败')
      visible.value = false
    }
  }
}

/** 提交表单 */
async function handleSubmit() {
  await formElRef.value?.validate()
  submitting.value = true
  try {
    const payload: OwnerCreateDTO | OwnerUpdateDTO = {
      ownerName: form.name,
      phone: form.phone,
      idCard: form.idCard,
      gender: form.gender,
      avatar: form.avatar || undefined,
      email: form.email || undefined,
      emergencyContact: form.emergencyContact || undefined,
      emergencyPhone: form.emergencyPhone || undefined
    }
    if (isEdit.value) {
      await updateOwner(editId.value, payload as OwnerUpdateDTO)
    } else {
      await createOwner(payload as OwnerCreateDTO)
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
