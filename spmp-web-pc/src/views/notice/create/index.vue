<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>发布公告</template>
      <el-form :model="form" ref="formRef" :rules="rules" label-width="120px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告类型" prop="noticeType">
          <el-radio-group v-model="form.noticeType">
            <el-radio label="NORMAL">通知公告</el-radio>
            <el-radio label="EMERGENCY">紧急公告</el-radio>
            <el-radio label="ACTIVITY">活动公告</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否置顶">
          <el-switch v-model="form.topFlag" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker v-model="form.expireTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择过期时间（可选）" style="width: 100%" />
        </el-form-item>
        <el-form-item label="推送范围" prop="scopeList">
          <div class="scope-editor">
            <div v-for="(scope, idx) in form.scopeList" :key="idx" class="scope-row">
              <el-select v-model="scope.scopeType" style="width: 160px" @change="onScopeTypeChange(scope)">
                <el-option label="全部业主" value="ALL" />
                <el-option label="指定小区" value="COMMUNITY" />
                <el-option label="指定楼栋" value="BUILDING" />
              </el-select>
              <el-select
                v-if="scope.scopeType === 'COMMUNITY'"
                v-model="scope.targetId"
                style="margin-left: 8px; width: 260px"
                placeholder="请选择小区"
                filterable
                clearable
              >
                <el-option v-for="item in communityOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
              <template v-if="scope.scopeType === 'BUILDING'">
                <el-select
                  v-model="scope.communityId"
                  style="margin-left: 8px; width: 180px"
                  placeholder="先选择小区"
                  filterable
                  clearable
                  @change="loadBuildingOptions(scope)"
                >
                  <el-option v-for="item in communityOptions" :key="item.id" :label="item.name" :value="item.id" />
                </el-select>
                <el-select
                  v-model="scope.targetId"
                  style="margin-left: 8px; width: 180px"
                  placeholder="再选择楼栋"
                  filterable
                  clearable
                >
                  <el-option
                    v-for="item in getScopeBuildings(scope)"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </template>
              <el-button link type="danger" @click="removeScope(idx)" style="margin-left: 8px">移除</el-button>
            </div>
            <el-button type="primary" plain size="small" @click="addScope">+ 添加范围</el-button>
          </div>
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="12" placeholder="支持 HTML 富文本片段，服务端会自动过滤不安全标签" />
          <div class="hint">提示：MVP 版本使用纯文本输入，后续可接入 TinyMCE/WangEditor 富文本编辑器。</div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">提交审批</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createNotice } from '@/api/notice/notice'
import type { NoticeCreateDTO } from '@/api/notice/notice'
import { getCascadeData, type CascadeItem } from '@/api/base/cascade'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const communityOptions = ref<CascadeItem[]>([])
const buildingOptionsMap = reactive<Record<number, CascadeItem[]>>({})

type ScopeFormItem = {
  scopeType: 'ALL' | 'COMMUNITY' | 'BUILDING'
  targetId?: number
  communityId?: number
}

const form = reactive<{
  title: string
  content: string
  noticeType: 'NORMAL' | 'EMERGENCY' | 'ACTIVITY'
  topFlag?: number
  expireTime?: string
  scopeList: ScopeFormItem[]
}>({
  title: '',
  content: '',
  noticeType: 'NORMAL',
  topFlag: 0,
  expireTime: undefined,
  scopeList: [{ scopeType: 'ALL' }]
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  noticeType: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }],
  scopeList: [
    {
      validator: (_rule, value, cb) => {
        if (!value || value.length === 0) return cb(new Error('请至少添加一个推送范围'))
        for (const s of value) {
          if (!s.scopeType) return cb(new Error('范围类型不能为空'))
          if (s.scopeType !== 'ALL' && !s.targetId) return cb(new Error('请完成推送范围选择'))
          if (s.scopeType === 'BUILDING' && !s.communityId) return cb(new Error('指定楼栋时请先选择小区'))
        }
        cb()
      },
      trigger: 'change'
    }
  ]
}

function addScope() {
  form.scopeList.push({ scopeType: 'COMMUNITY', targetId: undefined as unknown as number, communityId: undefined })
}

function removeScope(idx: number) {
  form.scopeList.splice(idx, 1)
}

function onScopeTypeChange(scope: ScopeFormItem) {
  scope.targetId = undefined
  if (scope.scopeType !== 'BUILDING') {
    scope.communityId = undefined
  }
}

async function ensureCommunities() {
  if (communityOptions.value.length) return
  communityOptions.value = await getCascadeData('COMMUNITY')
}

async function loadBuildingOptions(scope: ScopeFormItem) {
  scope.targetId = undefined
  if (!scope.communityId) return
  if (buildingOptionsMap[scope.communityId]) return
  buildingOptionsMap[scope.communityId] = await getCascadeData('BUILDING', scope.communityId)
}

function getScopeBuildings(scope: ScopeFormItem) {
  if (!scope.communityId) return []
  return buildingOptionsMap[scope.communityId] || []
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload: NoticeCreateDTO = {
      title: form.title,
      content: form.content,
      noticeType: form.noticeType,
      topFlag: form.topFlag,
      expireTime: form.expireTime,
      scopeList: form.scopeList.map((s) => ({
        scopeType: s.scopeType,
        targetId: s.scopeType === 'ALL' ? undefined : s.targetId
      }))
    }
    const res: any = await createNotice(payload)
    ElMessage.success(`提交成功，公告ID: ${res.data}`)
    router.push('/notice/list')
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.push('/notice/list')
}

ensureCommunities()
</script>

<style scoped>
.page-container {
  padding: 16px;
}
.scope-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}
.hint {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}
</style>
