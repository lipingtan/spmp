<template>
  <div class="page-container">
    <!-- 顶部返回按钮 -->
    <div class="page-header">
      <el-button @click="goBack" :icon="ArrowLeft">返回业主列表</el-button>
      <span class="owner-name">{{ ownerInfo.ownerName }}</span>
      <el-tag v-if="ownerInfo.ownerStatus" :type="statusTagType(ownerInfo.ownerStatus)" size="small">
        {{ statusText(ownerInfo.ownerStatus) }}
      </el-tag>
    </div>

    <!-- Tab 页签 -->
    <el-card shadow="never" v-loading="loading">
      <el-tabs v-model="activeTab">
        <!-- Tab 1：基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="姓名">{{ ownerInfo.ownerName }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ ownerInfo.phoneMasked }}</el-descriptions-item>
            <el-descriptions-item label="身份证号">{{ ownerInfo.idCardMasked }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ genderText(ownerInfo.gender) }}</el-descriptions-item>
            <el-descriptions-item label="头像">
              <el-avatar v-if="ownerInfo.avatar" :src="ownerInfo.avatar" :size="40" />
              <span v-else>未设置</span>
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ ownerInfo.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="紧急联系人">{{ ownerInfo.emergencyContact || '-' }}</el-descriptions-item>
            <el-descriptions-item label="紧急联系电话">{{ ownerInfo.emergencyPhoneMasked || '-' }}</el-descriptions-item>
            <el-descriptions-item label="业主状态">
              <el-tag :type="statusTagType(ownerInfo.ownerStatus)" size="small">
                {{ statusText(ownerInfo.ownerStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="来源">{{ sourceText(ownerInfo.ownerSource) }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ ownerInfo.createTime }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ ownerInfo.updateTime }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <!-- Tab 2：房产绑定列表 -->
        <el-tab-pane label="房产绑定" name="property">
          <div class="tab-toolbar">
            <el-button
              type="primary"
              size="small"
              v-if="hasPermission('owner:property:binding')"
              @click="handleBindProperty"
            >绑定房产</el-button>
          </div>
          <el-table :data="ownerInfo.propertyBindings" row-key="id">
            <el-table-column prop="communityName" label="小区" min-width="120" show-overflow-tooltip />
            <el-table-column prop="buildingName" label="楼栋" width="100" />
            <el-table-column prop="unitName" label="单元" width="80" />
            <el-table-column prop="houseName" label="房屋" width="100" />
            <el-table-column prop="relationType" label="关系类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag size="small">{{ relationTypeText(row.relationType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="bindTime" label="绑定时间" width="180" />
            <el-table-column prop="unbindTime" label="解绑时间" width="180">
              <template #default="{ row }">
                {{ row.unbindTime || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="!row.unbindTime && hasPermission('owner:property:unbind')"
                  link
                  type="danger"
                  size="small"
                  @click="handleUnbind(row)"
                >解除绑定</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- Tab 3：家庭成员列表 -->
        <el-tab-pane label="家庭成员" name="family">
          <el-table :data="ownerInfo.familyMembers" row-key="id">
            <el-table-column prop="name" label="姓名" min-width="100" />
            <el-table-column prop="phoneMasked" label="手机号" width="130" />
            <el-table-column prop="idCardMasked" label="身份证号" width="180" />
            <el-table-column prop="relation" label="与业主关系" width="120" align="center">
              <template #default="{ row }">
                {{ familyRelationText(row.relation) }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
          </el-table>
        </el-tab-pane>

        <!-- Tab 4：认证历史记录 -->
        <el-tab-pane label="认证历史" name="certification">
          <el-table :data="ownerInfo.certifications" row-key="id">
            <el-table-column prop="communityName" label="小区" min-width="120" show-overflow-tooltip />
            <el-table-column prop="buildingName" label="楼栋" width="100" />
            <el-table-column prop="unitName" label="单元" width="80" />
            <el-table-column prop="houseName" label="房屋" width="100" />
            <el-table-column prop="status" label="认证状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="certStatusTagType(row.status)" size="small">
                  {{ certStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="rejectReason" label="驳回原因" min-width="150" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.rejectReason || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="applyTime" label="申请时间" width="180" />
            <el-table-column prop="approveTime" label="审批时间" width="180">
              <template #default="{ row }">
                {{ row.approveTime || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="approverName" label="审批人" width="100">
              <template #default="{ row }">
                {{ row.approverName || '-' }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 房产绑定弹窗 -->
    <PropertyBindForm ref="bindFormRef" :owner-id="ownerId" @success="fetchDetail" />
  </div>
</template>

<script setup lang="ts">
/**
 * 业主详情页面
 * 功能：展示业主基本信息、房产绑定列表、家庭成员列表、认证历史记录
 * 房产绑定列表支持绑定/解绑操作
 */
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getOwner } from '@/api/owner/owner'
import { unbindProperty } from '@/api/owner/property-binding'
import { useUserStore } from '@/store/modules/user'
import PropertyBindForm from './PropertyBindForm.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

/** 业主 ID（从路由参数获取） */
const ownerId = Number(route.params.id)
/** 加载状态 */
const loading = ref(false)
/** 当前激活的 Tab */
const activeTab = ref('basic')
/** 房产绑定弹窗引用 */
const bindFormRef = ref<InstanceType<typeof PropertyBindForm>>()

/** 业主详情数据 */
const ownerInfo = ref<Record<string, any>>({
  ownerName: '',
  phoneMasked: '',
  idCardMasked: '',
  gender: 0,
  avatar: '',
  email: '',
  emergencyContact: '',
  emergencyPhoneMasked: '',
  ownerStatus: '',
  ownerSource: '',
  createTime: '',
  updateTime: '',
  propertyBindings: [],
  familyMembers: [],
  certifications: []
})

/** 权限检查 */
function hasPermission(perm: string): boolean {
  return userStore.hasPermission(perm)
}

/** 性别文本 */
function genderText(gender: number): string {
  const map: Record<number, string> = { 0: '未知', 1: '男', 2: '女' }
  return map[gender] ?? '未知'
}

/** 业主状态标签类型 */
function statusTagType(status: string): '' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<string, '' | 'success' | 'warning' | 'danger' | 'info'> = {
    UNCERTIFIED: 'info',
    CERTIFYING: 'warning',
    CERTIFIED: 'success',
    DISABLED: 'danger'
  }
  return map[status] ?? 'info'
}

/** 业主状态文本 */
function statusText(status: string): string {
  const map: Record<string, string> = {
    UNCERTIFIED: '未认证',
    CERTIFYING: '认证中',
    CERTIFIED: '已认证',
    DISABLED: '已停用'
  }
  return map[status] ?? status
}

/** 来源文本 */
function sourceText(source: string): string {
  const map: Record<string, string> = { ADMIN: '管理端录入', H5: 'H5 注册' }
  return map[source] ?? source
}

/** 关系类型文本 */
function relationTypeText(type: string): string {
  const map: Record<string, string> = { OWNER: '业主', TENANT: '租户', FAMILY: '家属' }
  return map[type] ?? type
}

/** 家庭关系文本 */
function familyRelationText(relation: string): string {
  const map: Record<string, string> = { SPOUSE: '配偶', PARENT: '父母', CHILD: '子女', OTHER: '其他' }
  return map[relation] ?? relation
}

/** 认证状态标签类型 */
function certStatusTagType(status: string): '' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<string, '' | 'success' | 'warning' | 'danger' | 'info'> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return map[status] ?? 'info'
}

/** 认证状态文本 */
function certStatusText(status: string): string {
  const map: Record<string, string> = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回' }
  return map[status] ?? status
}

/** 加载业主详情 */
async function fetchDetail() {
  loading.value = true
  try {
    const res: any = await getOwner(ownerId)
    const data = res.data || res
    ownerInfo.value = {
      ownerName: data.name || data.ownerName || '',
      phoneMasked: data.phoneMasked || '',
      idCardMasked: data.idCardMasked || '',
      gender: data.gender ?? 0,
      avatar: data.avatar || '',
      email: data.email || '',
      emergencyContact: data.emergencyContact || '',
      emergencyPhoneMasked: data.emergencyPhoneMasked || data.emergencyPhone || '',
      ownerStatus: data.status || data.ownerStatus || '',
      ownerSource: data.source || data.ownerSource || '',
      createTime: data.createTime || '',
      updateTime: data.updateTime || '',
      propertyBindings: data.propertyBindings || [],
      familyMembers: data.familyMembers || [],
      certifications: data.certifications || []
    }
  } catch {
    ElMessage.error('加载业主详情失败')
  } finally {
    loading.value = false
  }
}

/** 返回业主列表 */
function goBack() {
  router.push('/owner/owners')
}

/** 绑定房产（打开绑定弹窗） */
function handleBindProperty() {
  bindFormRef.value?.open()
}

/** 解除绑定 */
async function handleUnbind(row: any) {
  await ElMessageBox.confirm(
    `确认解除与「${row.communityName} ${row.buildingName} ${row.unitName} ${row.houseName}」的绑定关系？`,
    '解除绑定确认',
    { type: 'warning' }
  )
  await unbindProperty(row.id)
  ElMessage.success('解除绑定成功')
  fetchDetail()
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.owner-name {
  font-size: 18px;
  font-weight: 600;
}
.tab-toolbar {
  margin-bottom: 12px;
}
</style>
