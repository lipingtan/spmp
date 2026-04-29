<template>
  <el-dialog v-model="visible" title="配置数据权限" width="500px" destroy-on-close>
    <el-form label-width="100px">
      <el-form-item label="权限级别">
        <el-select v-model="level" @change="handleLevelChange">
          <el-option label="全部" value="ALL" />
          <el-option label="片区" value="AREA" />
          <el-option label="小区" value="COMMUNITY" />
          <el-option label="楼栋" value="BUILDING" />
          <el-option label="仅本人" value="SELF" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="showDataSelect" :label="dataLabel">
        <el-select v-model="selectedIds" multiple placeholder="请选择" style="width: 100%">
          <el-option v-for="item in dataOptions" :key="item.id" :label="item.name" :value="item.id" />
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
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { configDataPermission } from '@/api/role'
import { getDataPermissionOptions } from '@/api/data-permission'
import type { DataPermissionOptions, OptionItem } from '@/api/data-permission'

const visible = ref(false)
const submitting = ref(false)
const roleId = ref(0)
const level = ref('SELF')
const selectedIds = ref<number[]>([])
const options = ref<DataPermissionOptions>({ areas: [], communities: [], buildings: [] })

const showDataSelect = computed(() => ['AREA', 'COMMUNITY', 'BUILDING'].includes(level.value))
const dataLabel = computed(() => ({ AREA: '片区', COMMUNITY: '小区', BUILDING: '楼栋' }[level.value] || ''))
const dataOptions = computed<OptionItem[]>(() => {
  if (level.value === 'AREA') return options.value.areas
  if (level.value === 'COMMUNITY') return options.value.communities
  if (level.value === 'BUILDING') return options.value.buildings
  return []
})

function handleLevelChange() { selectedIds.value = [] }

async function open(id: number, currentLevel: string) {
  roleId.value = id
  level.value = currentLevel || 'SELF'
  selectedIds.value = []
  visible.value = true
  options.value = await getDataPermissionOptions()
}

async function handleSubmit() {
  submitting.value = true
  try {
    await configDataPermission(roleId.value, { dataPermissionLevel: level.value, dataIds: selectedIds.value })
    ElMessage.success('数据权限配置成功')
    visible.value = false
  } finally { submitting.value = false }
}

defineExpose({ open })
</script>
