<template>
  <el-dialog v-model="visible" :title="isEdit ? '编辑小区' : '新增小区'" width="600px" destroy-on-close>
    <el-form ref="formElRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="小区名称" prop="communityName">
        <el-input v-model="form.communityName" placeholder="请输入小区名称" maxlength="128" />
      </el-form-item>
      <el-form-item label="小区编码" prop="communityCode">
        <el-input v-model="form.communityCode" placeholder="留空则自动生成" maxlength="20" :disabled="isEdit" />
      </el-form-item>
      <el-form-item label="所属片区" prop="districtId">
        <el-select v-model="form.districtId" placeholder="请选择所属片区" style="width: 100%" :disabled="isEdit">
          <el-option v-for="d in districtOptions" :key="d.id" :label="d.name" :value="d.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="小区地址" prop="address">
        <el-input v-model="form.address" placeholder="请输入小区地址" maxlength="256" />
      </el-form-item>
      <el-form-item label="联系电话" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="20" />
      </el-form-item>
      <el-form-item label="物业公司" prop="propertyCompany">
        <el-input v-model="form.propertyCompany" placeholder="请输入物业公司" maxlength="128" />
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="小区面积" prop="area">
            <el-input-number v-model="form.area" :min="0" :precision="2" placeholder="平方米" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="经度" prop="longitude">
            <el-input-number v-model="form.longitude" :precision="7" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="纬度" prop="latitude">
            <el-input-number v-model="form.latitude" :precision="7" style="width: 100%" />
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
import { ref, reactive, nextTick } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createCommunity, updateCommunity } from '@/api/base/community'
import type { CascadeItem } from '@/api/base/cascade'
import { useBaseStore } from '@/store/modules/base'

defineProps<{ districtOptions: CascadeItem[] }>()
const emit = defineEmits<{ success: [] }>()
const baseStore = useBaseStore()

const visible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const editId = ref<number>(0)
const formElRef = ref<FormInstance>()

const form = reactive({
  communityName: '',
  communityCode: '',
  districtId: undefined as number | undefined,
  address: '',
  phone: '',
  propertyCompany: '',
  area: undefined as number | undefined,
  longitude: undefined as number | undefined,
  latitude: undefined as number | undefined,
  remark: ''
})

const rules: FormRules = {
  communityName: [{ required: true, message: '请输入小区名称', trigger: 'blur' }],
  districtId: [{ required: true, message: '请选择所属片区', trigger: 'change' }],
  address: [{ required: true, message: '请输入小区地址', trigger: 'blur' }]
}

async function open(row?: any) {
  visible.value = true
  isEdit.value = !!row
  await nextTick()
  formElRef.value?.resetFields()
  if (row) {
    editId.value = row.id
    form.communityName = row.communityName
    form.communityCode = row.communityCode
    form.districtId = row.districtId
    form.address = row.address
    form.phone = row.phone || ''
    form.propertyCompany = row.propertyCompany || ''
    form.area = row.area
    form.longitude = row.longitude
    form.latitude = row.latitude
    form.remark = row.remark || ''
  } else {
    editId.value = 0
    Object.assign(form, { communityName: '', communityCode: '', districtId: undefined, address: '', phone: '', propertyCompany: '', area: undefined, longitude: undefined, latitude: undefined, remark: '' })
  }
}

async function handleSubmit() {
  await formElRef.value?.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateCommunity(editId.value, {
        communityName: form.communityName, address: form.address, phone: form.phone,
        propertyCompany: form.propertyCompany, area: form.area, longitude: form.longitude,
        latitude: form.latitude, remark: form.remark
      })
    } else {
      await createCommunity({
        communityName: form.communityName, communityCode: form.communityCode || undefined,
        address: form.address, districtId: form.districtId!, phone: form.phone,
        propertyCompany: form.propertyCompany, area: form.area, longitude: form.longitude,
        latitude: form.latitude, remark: form.remark
      })
    }
    ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
    visible.value = false
    baseStore.clearCache()
    emit('success')
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>
