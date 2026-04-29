<template>
  <el-dialog v-model="visible" title="分配菜单权限" width="500px" destroy-on-close>
    <el-tree ref="treeRef" :data="menuTree" show-checkbox node-key="id" :default-checked-keys="checkedKeys"
      :props="{ label: 'menuName', children: 'children' }" />
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { ElTree } from 'element-plus'
import { getMenuTree } from '@/api/menu'
import { getRoleMenuIds, assignMenus } from '@/api/role'
import type { MenuTreeItem } from '@/api/menu'

const visible = ref(false)
const submitting = ref(false)
const roleId = ref(0)
const menuTree = ref<MenuTreeItem[]>([])
const checkedKeys = ref<number[]>([])
const treeRef = ref<InstanceType<typeof ElTree>>()

async function open(id: number) {
  roleId.value = id
  visible.value = true
  const [tree, ids] = await Promise.all([getMenuTree(), getRoleMenuIds(id)])
  menuTree.value = tree
  // 只勾选叶子节点，避免父节点自动全选
  checkedKeys.value = filterLeafIds(tree, ids)
}

function filterLeafIds(tree: MenuTreeItem[], ids: number[]): number[] {
  const leafIds: number[] = []
  function walk(nodes: MenuTreeItem[]) {
    for (const n of nodes) {
      if (!n.children || n.children.length === 0) {
        if (ids.includes(n.id)) leafIds.push(n.id)
      } else {
        walk(n.children)
      }
    }
  }
  walk(tree)
  return leafIds
}

async function handleSubmit() {
  submitting.value = true
  try {
    const checked = treeRef.value?.getCheckedKeys(false) as number[]
    const half = treeRef.value?.getHalfCheckedKeys() as number[]
    await assignMenus(roleId.value, [...checked, ...half])
    ElMessage.success('菜单权限分配成功')
    visible.value = false
  } finally { submitting.value = false }
}

defineExpose({ open })
</script>
