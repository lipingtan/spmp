<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>&#36153;&#29992;&#37197;&#32622;</template>
      <el-button type="primary" @click="openCreate" style="margin-bottom: 10px">&#26032;&#22686;</el-button>
      <el-table :data="rows" v-loading="loading">
        <el-table-column prop="communityId" label="&#23567;&#21306;ID" min-width="100" />
        <el-table-column prop="buildingId" label="&#27004;&#26635;ID" min-width="100" />
        <el-table-column label="&#36153;&#29992;&#31867;&#22411;" min-width="120">
          <template #default="scope">{{ feeTypeName(scope.row.feeType) }}</template>
        </el-table-column>
        <el-table-column label="&#35745;&#36153;&#26041;&#24335;" min-width="120">
          <template #default="scope">{{ billingMethodName(scope.row.billingMethod) }}</template>
        </el-table-column>
        <el-table-column prop="unitPrice" label="&#21333;&#20215;" min-width="100" />
        <el-table-column label="&#29366;&#24577;" min-width="100">
          <template #default="scope">{{ statusName(scope.row.status) }}</template>
        </el-table-column>
        <el-table-column label="&#25805;&#20316;" min-width="200">
          <template #default="scope">
            <el-button link @click="openEdit(scope.row)">&#32534;&#36753;</el-button>
            <el-button link type="danger" @click="remove(scope.row.id)">&#21024;&#38500;</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" title="&#36153;&#29992;&#37197;&#32622;" width="500px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="&#23567;&#21306;">
          <el-select v-model="form.communityId" style="width:100%" clearable filterable @change="onCommunityChange">
            <el-option v-for="item in communityOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="&#27004;&#26635;">
          <el-select v-model="form.buildingId" style="width:100%" clearable filterable>
            <el-option v-for="item in buildingOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="&#36153;&#29992;&#31867;&#22411;"><el-select v-model="form.feeType" style="width:100%">
          <el-option label="&#29289;&#19994;&#36153;" value="PROPERTY_FEE" />
          <el-option label="&#20572;&#36710;&#36153;" value="PARKING_FEE" />
          <el-option label="&#27700;&#36153;" value="WATER_FEE" />
          <el-option label="&#30005;&#36153;" value="ELECTRICITY_FEE" />
          <el-option label="&#29123;&#27668;&#36153;" value="GAS_FEE" />
        </el-select></el-form-item>
        <el-form-item label="&#35745;&#36153;&#26041;&#24335;"><el-select v-model="form.billingMethod" style="width:100%">
          <el-option label="&#25353;&#38754;&#31215;&#35745;&#36153;" value="BY_AREA" />
          <el-option label="&#22266;&#23450;&#26376;&#36153;" value="FIXED" />
          <el-option label="&#25353;&#29992;&#37327;&#35745;&#36153;" value="BY_USAGE" />
        </el-select></el-form-item>
        <el-form-item label="&#21333;&#20215;"><el-input-number v-model="form.unitPrice" :min="0.0001" :precision="4" style="width:100%" /></el-form-item>
        <el-form-item label="&#25130;&#27490;&#26085;"><el-input-number v-model="form.dueDay" :min="1" :max="28" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false">&#21462;&#28040;</el-button>
        <el-button type="primary" @click="submit">&#30830;&#35748;</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listFeeConfigs } from '@/api/billing/billing'
import request from '@/utils/request'
import { getCascadeData, type CascadeItem } from '@/api/base/cascade'

const loading = ref(false)
const rows = ref<any[]>([])
const visible = ref(false)
const editId = ref<number | null>(null)
const form = ref<any>({ communityId: 1, buildingId: null, feeType: 'PROPERTY_FEE', billingMethod: 'BY_AREA', unitPrice: 1, dueDay: 15 })
const communityOptions = ref<CascadeItem[]>([])
const buildingOptions = ref<CascadeItem[]>([])

const feeTypeName = (v?: string) =>
  ({
    PROPERTY_FEE: '\u7269\u4e1a\u8d39',
    PARKING_FEE: '\u505c\u8f66\u8d39',
    WATER_FEE: '\u6c34\u8d39',
    ELECTRICITY_FEE: '\u7535\u8d39',
    GAS_FEE: '\u71c3\u6c14\u8d39'
  } as any)[v || ''] || v || '-'

const billingMethodName = (v?: string) =>
  ({
    BY_AREA: '\u6309\u9762\u79ef\u8ba1\u8d39',
    FIXED: '\u56fa\u5b9a\u6708\u8d39',
    BY_USAGE: '\u6309\u7528\u91cf\u8ba1\u8d39'
  } as any)[v || ''] || v || '-'

const statusName = (v?: string) =>
  ({
    ENABLED: '\u542f\u7528',
    DISABLED: '\u505c\u7528'
  } as any)[v || ''] || v || '-'

const loadCommunityOptions = async () => {
  communityOptions.value = await getCascadeData('COMMUNITY')
}

const loadBuildingOptions = async (communityId?: number) => {
  buildingOptions.value = []
  if (!communityId) return
  buildingOptions.value = await getCascadeData('BUILDING', communityId)
}

const load = async () => {
  loading.value = true
  try {
    const data: any = await listFeeConfigs({})
    rows.value = Array.isArray(data) ? data : data?.data || []
  } finally { loading.value = false }
}

const openCreate = async () => {
  editId.value = null
  form.value = { communityId: undefined, buildingId: null, feeType: 'PROPERTY_FEE', billingMethod: 'BY_AREA', unitPrice: 1, dueDay: 15 }
  await loadCommunityOptions()
  buildingOptions.value = []
  visible.value = true
}
const openEdit = async (row: any) => {
  editId.value = row.id
  form.value = { ...row }
  await loadCommunityOptions()
  await loadBuildingOptions(form.value.communityId)
  visible.value = true
}
const onCommunityChange = async (communityId?: number) => {
  form.value.buildingId = null
  await loadBuildingOptions(communityId)
}
const submit = async () => {
  if (!form.value.communityId) {
    ElMessage.warning('\u8bf7\u9009\u62e9\u5c0f\u533a')
    return
  }
  if (!form.value.buildingId) {
    ElMessage.warning('\u8bf7\u9009\u62e9\u697c\u680b')
    return
  }
  if (editId.value) {
    await request.put(`/billing/config/${editId.value}`, form.value)
  } else {
    await request.post('/billing/config', form.value)
  }
  ElMessage.success('\u64cd\u4f5c\u6210\u529f')
  visible.value = false
  await load()
}
const remove = async (id: number) => {
  await request.delete(`/billing/config/${id}`)
  ElMessage.success('\u5220\u9664\u6210\u529f')
  await load()
}

load()
</script>
