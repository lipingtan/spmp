<template>
  <div class="page-container">
    <!-- 个人信息 -->
    <el-card shadow="never" class="profile-card">
      <template #header><span>个人信息</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ profile.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ profile.realName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ profile.phone }}</el-descriptions-item>
        <el-descriptions-item label="数据权限">{{ profile.dataPermissionLevel }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag v-for="r in profile.roles" :key="r" size="small" class="role-tag">{{ r }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <div class="action-row">
        <el-button type="primary" @click="showEditDialog = true">修改信息</el-button>
        <el-button @click="showPwdDialog = true">修改密码</el-button>
      </div>
    </el-card>

    <!-- 修改信息对话框 -->
    <el-dialog v-model="showEditDialog" title="修改个人信息" width="450px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="editForm.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleEditSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPwdDialog" title="修改密码" width="450px" destroy-on-close>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPwdDialog = false">取消</el-button>
        <el-button type="primary" :loading="pwdLoading" @click="handlePwdSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile, updatePassword } from '@/api/profile'
import type { ProfileInfo } from '@/api/profile'

const profile = ref<ProfileInfo>({ userId: 0, username: '', realName: '', phone: '', avatar: '', roles: [], permissions: [], dataPermissionLevel: '' })

const showEditDialog = ref(false)
const editLoading = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive({ realName: '', phone: '' })
const editRules: FormRules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const showPwdDialog = ref(false)
const pwdLoading = ref(false)
const pwdFormRef = ref<FormInstance>()
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 8, message: '密码至少8位', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (_r: any, v: string, cb: any) => v === pwdForm.newPassword ? cb() : cb(new Error('两次密码不一致')), trigger: 'blur' }
  ]
}

async function fetchProfile() {
  profile.value = await getProfile()
  editForm.realName = profile.value.realName
  editForm.phone = ''
}

async function handleEditSubmit() {
  await editFormRef.value?.validate()
  editLoading.value = true
  try {
    await updateProfile({ realName: editForm.realName, phone: editForm.phone || undefined })
    ElMessage.success('修改成功')
    showEditDialog.value = false
    fetchProfile()
  } finally { editLoading.value = false }
}

async function handlePwdSubmit() {
  await pwdFormRef.value?.validate()
  pwdLoading.value = true
  try {
    await updatePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    showPwdDialog.value = false
    localStorage.removeItem('access_token')
    window.location.href = '/login'
  } finally { pwdLoading.value = false }
}

onMounted(fetchProfile)
</script>

<style scoped>
.page-container { padding: 16px; }
.profile-card { max-width: 800px; }
.action-row { margin-top: 20px; }
.role-tag { margin-right: 4px; }
</style>
