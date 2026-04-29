<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>&#36134;&#21333;&#31649;&#29702;</template>
      <el-form inline>
        <el-form-item label="&#20851;&#38190;&#23383;">
          <el-input v-model="query.keyword" placeholder="&#36134;&#21333;&#21495;/&#19994;&#20027;&#22995;&#21517;" clearable />
        </el-form-item>
        <el-form-item label="&#29366;&#24577;">
          <el-select v-model="query.status" placeholder="&#20840;&#37096;" clearable style="width: 140px">
            <el-option label="&#24453;&#32564;&#36153;" value="UNPAID" />
            <el-option label="&#24453;&#25903;&#20184;" value="PAYING" />
            <el-option label="&#24050;&#32564;&#36153;" value="PAID" />
            <el-option label="&#24050;&#36894;&#26399;" value="OVERDUE" />
          </el-select>
        </el-form-item>
        <el-button type="primary" @click="search">&#26597;&#35810;</el-button>
        <el-button @click="openGenerate">&#25209;&#37327;&#29983;&#25104;</el-button>
        <el-button @click="openCreate">&#25163;&#21160;&#21019;&#24314;</el-button>
      </el-form>
      <el-table :data="rows" style="width: 100%" v-loading="loading">
        <el-table-column prop="billNo" label="&#36134;&#21333;&#32534;&#21495;" min-width="180" />
        <el-table-column prop="ownerName" label="&#19994;&#20027;" min-width="120" />
        <el-table-column label="&#36153;&#29992;&#31867;&#22411;" min-width="120">
          <template #default="scope">
            {{ safeFeeTypeName(scope.row.feeTypeName, scope.row.feeType) }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="&#24212;&#25910;&#37329;&#39069;" min-width="120" />
        <el-table-column label="&#29366;&#24577;" min-width="100">
          <template #default="scope">
            {{ safeStatusName(scope.row.statusName, scope.row.status) }}
          </template>
        </el-table-column>
        <el-table-column prop="billingPeriod" label="&#36134;&#26399;" min-width="100" />
        <el-table-column label="&#32564;&#36153;&#26102;&#38388;" min-width="170">
          <template #default="scope">
            {{ scope.row.paidTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="&#25805;&#20316;" min-width="320">
          <template #default="scope">
            <el-button link type="primary" @click="showDetail(scope.row.id)">&#35814;&#24773;</el-button>
            <el-button link type="warning" @click="doReduce(scope.row.id)">&#20943;&#20813;</el-button>
            <el-button link type="danger" @click="doCancel(scope.row.id)">&#21462;&#28040;</el-button>
            <el-button link :disabled="scope.row.status !== 'PAID'" @click="doRefund(scope.row.id)">&#36864;&#27454;</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="&#36134;&#21333;&#35814;&#24773;" width="700px">
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="&#36134;&#21333;&#32534;&#21495;">{{ detail.billNo }}</el-descriptions-item>
        <el-descriptions-item label="&#29366;&#24577;">{{ safeStatusName(detail.statusName, detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="&#19994;&#20027;">{{ detail.ownerName }}</el-descriptions-item>
        <el-descriptions-item label="&#36153;&#29992;&#31867;&#22411;">{{ safeFeeTypeName(detail.feeTypeName, detail.feeType) }}</el-descriptions-item>
        <el-descriptions-item label="&#36134;&#26399;">{{ detail.billingPeriod }}</el-descriptions-item>
        <el-descriptions-item label="&#24212;&#25910;&#37329;&#39069;">{{ detail.amount }}</el-descriptions-item>
        <el-descriptions-item label="&#24050;&#32564;&#37329;&#39069;">{{ detail.paidAmount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="&#24050;&#36864;&#37329;&#39069;">{{ detail.refundAmount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="&#25130;&#27490;&#26085;&#26399;">{{ detail.dueDate }}</el-descriptions-item>
        <el-descriptions-item label="&#32564;&#36153;&#26102;&#38388;">{{ detail.paidTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="&#25151;&#23627;">{{ detail.houseFullName || detail.houseId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="&#23567;&#21306;">{{ detail.communityName || detail.communityId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="&#27004;&#26635;">{{ detail.buildingName || detail.buildingId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="&#22791;&#27880;" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="generateVisible" title="&#25209;&#37327;&#29983;&#25104;" width="480px" @open="onOpenGenerate">
      <el-form :model="generateForm" label-width="120px">
        <el-form-item label="&#23567;&#21306;">
          <el-select v-model="generateCommunityId" style="width:100%" clearable @change="loadBuildingsForGenerate">
            <el-option v-for="item in communityOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="&#27004;&#26635;">
          <el-select v-model="generateForm.buildingId" style="width:100%" clearable>
            <el-option v-for="item in generateBuildingOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="&#36153;&#29992;&#31867;&#22411;">
          <el-select v-model="generateForm.feeType" style="width:100%">
            <el-option label="&#29289;&#19994;&#36153;" value="PROPERTY_FEE" />
            <el-option label="&#20572;&#36710;&#36153;" value="PARKING_FEE" />
            <el-option label="&#27700;&#36153;" value="WATER_FEE" />
            <el-option label="&#30005;&#36153;" value="ELECTRICITY_FEE" />
            <el-option label="&#29123;&#27668;&#36153;" value="GAS_FEE" />
          </el-select>
        </el-form-item>
        <el-form-item label="&#36134;&#26399;" required>
          <el-date-picker
            v-model="generateForm.billingPeriod"
            type="month"
            value-format="YYYY-MM"
            format="YYYY-MM"
            :clearable="false"
            placeholder="&#36873;&#25321;&#36134;&#26399;&#26376;&#20221;"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateVisible=false">&#21462;&#28040;</el-button>
        <el-button type="primary" @click="submitGenerate">&#30830;&#35748;&#29983;&#25104;</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="createVisible" title="&#25163;&#21160;&#21019;&#24314;&#36134;&#21333;" width="560px">
      <el-form :model="createForm" label-width="120px">
        <el-form-item label="&#23567;&#21306;">
          <el-select v-model="createForm.communityId" style="width:100%" clearable @change="loadBuildingsForCreate">
            <el-option v-for="item in communityOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="&#27004;&#26635;">
          <el-select v-model="createForm.buildingId" style="width:100%" clearable @change="loadOwnersAndHouses">
            <el-option v-for="item in createBuildingOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="&#25151;&#23627;">
          <el-select v-model="createForm.houseId" style="width:100%" clearable filterable>
            <el-option
              v-for="item in houseOptions"
              :key="item.id"
              :label="`${item.houseCode}${item.unitName ? ' / ' + item.unitName : ''}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="&#19994;&#20027;">
          <el-select v-model="createForm.ownerId" style="width:100%" clearable filterable>
            <el-option v-for="item in ownerOptions" :key="item.id" :label="`${item.ownerName} (${item.phoneMasked || '-'})`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="&#36153;&#29992;&#31867;&#22411;">
          <el-select v-model="createForm.feeType" style="width:100%">
            <el-option label="&#29289;&#19994;&#36153;" value="PROPERTY_FEE" />
            <el-option label="&#20572;&#36710;&#36153;" value="PARKING_FEE" />
            <el-option label="&#27700;&#36153;" value="WATER_FEE" />
            <el-option label="&#30005;&#36153;" value="ELECTRICITY_FEE" />
            <el-option label="&#29123;&#27668;&#36153;" value="GAS_FEE" />
          </el-select>
        </el-form-item>
        <el-form-item label="&#36134;&#26399;" required>
          <el-date-picker
            v-model="createForm.billingPeriod"
            type="month"
            value-format="YYYY-MM"
            format="YYYY-MM"
            :clearable="false"
            placeholder="&#36873;&#25321;&#36134;&#26399;&#26376;&#20221;"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="&#37329;&#39069;"><el-input-number v-model="createForm.amount" :min="0.01" :precision="2" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible=false">&#21462;&#28040;</el-button>
        <el-button type="primary" @click="submitCreate">&#30830;&#35748;&#21019;&#24314;</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="refundVisible" title="&#36134;&#21333;&#36864;&#27454;" width="520px">
      <el-form :model="refundForm" label-width="120px">
        <el-form-item label="&#36864;&#27454;&#37329;&#39069;">
          <el-input-number
            v-model="refundForm.refundAmount"
            :min="0.01"
            :precision="2"
            :max="refundMaxAmount"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="&#36864;&#27454;&#21407;&#22240;">
          <el-input
            v-model="refundForm.refundReason"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="&#35831;&#36755;&#20837;&#36864;&#27454;&#21407;&#22240;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible=false">&#21462;&#28040;</el-button>
        <el-button type="primary" @click="submitRefund">&#30830;&#35748;&#36864;&#27454;</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listBills, getBillDetail, generateBills, createBill, applyReduce, approveReduce, cancelBill, refundBill,
  type BillListVO, type BillGenerateDTO, type BillCreateDTO, type BillDetailVO
} from '@/api/billing/billing'
import { getCascadeData, type CascadeItem } from '@/api/base/cascade'
import { listHouses, type HousePageItem } from '@/api/base/house'
import { listOwners, type OwnerListVO } from '@/api/owner/owner'

const loading = ref(false)
const rows = ref<BillListVO[]>([])
const query = ref({ pageNum: 1, pageSize: 10, keyword: '', status: '' })

const detailVisible = ref(false)
const detail = ref<BillDetailVO | null>(null)
const generateVisible = ref(false)
const createVisible = ref(false)
const refundVisible = ref(false)
const refundBillId = ref<number | null>(null)
const refundMaxAmount = ref<number>(99999999)

const currentBillingPeriod = () => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  return `${y}-${m}`
}

const generateForm = ref<BillGenerateDTO>({ feeType: 'PROPERTY_FEE', billingPeriod: currentBillingPeriod() })
const createForm = ref<BillCreateDTO>({
  ownerId: undefined as unknown as number,
  communityId: undefined as unknown as number,
  buildingId: undefined,
  houseId: undefined as unknown as number,
  feeType: 'PROPERTY_FEE',
  billingPeriod: currentBillingPeriod(),
  amount: 1
})
const refundForm = ref({ refundAmount: 0.01, refundReason: '' })
const generateCommunityId = ref<number | undefined>(undefined)
const communityOptions = ref<CascadeItem[]>([])
const generateBuildingOptions = ref<CascadeItem[]>([])
const createBuildingOptions = ref<CascadeItem[]>([])
const houseOptions = ref<HousePageItem[]>([])
const ownerOptions = ref<OwnerListVO[]>([])

const feeTypeName = (v?: string) =>
  ({
    PROPERTY_FEE: '\u7269\u4e1a\u8d39',
    PARKING_FEE: '\u505c\u8f66\u8d39',
    WATER_FEE: '\u6c34\u8d39',
    ELECTRICITY_FEE: '\u7535\u8d39',
    GAS_FEE: '\u71c3\u6c14\u8d39'
  } as any)[v || ''] || v || '-'

const statusName = (v?: string) =>
  ({
    UNPAID: '\u5f85\u7f34\u8d39',
    PAYING: '\u5f85\u652f\u4ed8',
    PAID: '\u5df2\u7f34\u8d39',
    OVERDUE: '\u5df2\u903e\u671f',
    REDUCED: '\u5df2\u51cf\u514d',
    REFUNDED: '\u5df2\u9000\u6b3e',
    CANCELLED: '\u5df2\u53d6\u6d88'
  } as any)[v || ''] || v || '-'

const isGarbled = (v?: string) => !!v && /[??]{2,}/.test(v)
const safeFeeTypeName = (label?: string, code?: string) => (label && !isGarbled(label) ? label : feeTypeName(code))
const safeStatusName = (label?: string, code?: string) => (label && !isGarbled(label) ? label : statusName(code))

const ensureCommunities = async () => {
  if (communityOptions.value.length) return
  communityOptions.value = await getCascadeData('COMMUNITY')
}

const loadBuildingsForGenerate = async () => {
  generateForm.value.buildingId = undefined
  generateBuildingOptions.value = []
  if (!generateCommunityId.value) return
  generateBuildingOptions.value = await getCascadeData('BUILDING', generateCommunityId.value)
}

const loadBuildingsForCreate = async () => {
  createForm.value.buildingId = undefined
  createForm.value.houseId = undefined as unknown as number
  createForm.value.ownerId = undefined as unknown as number
  createBuildingOptions.value = []
  houseOptions.value = []
  ownerOptions.value = []
  if (!createForm.value.communityId) return
  createBuildingOptions.value = await getCascadeData('BUILDING', createForm.value.communityId)
}

const loadOwnersAndHouses = async () => {
  createForm.value.houseId = undefined as unknown as number
  createForm.value.ownerId = undefined as unknown as number
  houseOptions.value = []
  ownerOptions.value = []
  if (!createForm.value.communityId || !createForm.value.buildingId) return
  const [houseRes, ownerRes] = await Promise.all([
    listHouses({
      communityId: createForm.value.communityId,
      buildingId: createForm.value.buildingId,
      pageNum: 1,
      pageSize: 200
    }) as Promise<any>,
    listOwners({
      communityId: createForm.value.communityId,
      buildingId: createForm.value.buildingId,
      pageNum: 1,
      pageSize: 200
    }) as Promise<any>
  ])
  houseOptions.value = houseRes?.data || []
  ownerOptions.value = ownerRes?.data || []
}

const search = async () => {
  loading.value = true
  try {
    const res: any = await listBills(query.value)
    rows.value = res?.data || []
  } finally {
    loading.value = false
  }
}

const showDetail = async (id: number) => {
  detail.value = await getBillDetail(id)
  detailVisible.value = true
}

const openGenerate = () => { generateVisible.value = true }
const onOpenGenerate = async () => {
  await ensureCommunities()
  if (!generateForm.value.billingPeriod) {
    generateForm.value.billingPeriod = currentBillingPeriod()
  }
  if (generateCommunityId.value) {
    await loadBuildingsForGenerate()
  }
}
const submitGenerate = async () => {
  if (!generateForm.value.billingPeriod) {
    ElMessage.warning('\u8bf7\u9009\u62e9\u8d26\u671f\u6708\u4efd')
    return
  }
  const batchNo = await generateBills(generateForm.value)
  ElMessage.success(`\u6279\u6b21\u53f7\uff1a${batchNo}`)
  generateVisible.value = false
}

const openCreate = async () => {
  await ensureCommunities()
  createForm.value = {
    ownerId: undefined as unknown as number,
    communityId: undefined as unknown as number,
    buildingId: undefined,
    houseId: undefined as unknown as number,
    feeType: 'PROPERTY_FEE',
    billingPeriod: currentBillingPeriod(),
    amount: 1
  }
  createBuildingOptions.value = []
  houseOptions.value = []
  ownerOptions.value = []
  createVisible.value = true
}
const submitCreate = async () => {
  if (!createForm.value.communityId || !createForm.value.buildingId || !createForm.value.houseId || !createForm.value.ownerId) {
    ElMessage.warning('\u8bf7\u5148\u9009\u62e9\u5c0f\u533a/\u697c\u680b/\u623f\u5c4b/\u4e1a\u4e3b')
    return
  }
  if (!createForm.value.billingPeriod) {
    ElMessage.warning('\u8bf7\u9009\u62e9\u8d26\u671f\u6708\u4efd')
    return
  }
  await createBill(createForm.value)
  ElMessage.success('\u521b\u5efa\u6210\u529f')
  createVisible.value = false
  await search()
}

const doReduce = async (id: number) => {
  const { value } = await ElMessageBox.prompt('\u8bf7\u8f93\u5165\u51cf\u514d\u91d1\u989d', '\u8d26\u5355\u51cf\u514d')
  await applyReduce(id, { reduceAmount: Number(value), reason: '\u624b\u52a8\u51cf\u514d' })
  await approveReduce(id, { approved: true, approveRemark: '\u624b\u52a8\u786e\u8ba4\u51cf\u514d' })
  ElMessage.success('\u64cd\u4f5c\u6210\u529f')
  await search()
}
const doCancel = async (id: number) => {
  await cancelBill(id, { cancelReason: '\u624b\u52a8\u53d6\u6d88' })
  ElMessage.success('\u64cd\u4f5c\u6210\u529f')
  await search()
}
const doRefund = async (id: number) => {
  const detailData = await getBillDetail(id)
  if (detailData.status !== 'PAID') {
    ElMessage.warning('\u4ec5\u5df2\u7f34\u8d39\u8d26\u5355\u53ef\u9000\u6b3e')
    return
  }
  const maxRefund = Number(detailData.paidAmount || 0) - Number(detailData.refundAmount || 0)
  if (maxRefund <= 0) {
    ElMessage.warning('\u5f53\u524d\u8d26\u5355\u65e0\u53ef\u9000\u91d1\u989d')
    return
  }
  refundBillId.value = id
  refundMaxAmount.value = Number(maxRefund.toFixed(2))
  refundForm.value = { refundAmount: Number(Math.min(0.01, refundMaxAmount.value).toFixed(2)), refundReason: '' }
  refundVisible.value = true
}

const submitRefund = async () => {
  if (!refundBillId.value) return
  if (!refundForm.value.refundAmount || Number(refundForm.value.refundAmount) <= 0) {
    ElMessage.warning('\u8bf7\u8f93\u5165\u5927\u4e8e0\u7684\u9000\u6b3e\u91d1\u989d')
    return
  }
  if (!refundForm.value.refundReason?.trim()) {
    ElMessage.warning('\u8bf7\u8f93\u5165\u9000\u6b3e\u539f\u56e0')
    return
  }
  await refundBill(refundBillId.value, {
    refundAmount: Number(refundForm.value.refundAmount),
    refundReason: refundForm.value.refundReason.trim()
  })
  ElMessage.success('\u9000\u6b3e\u6210\u529f')
  refundVisible.value = false
  await search()
}

search()
</script>
