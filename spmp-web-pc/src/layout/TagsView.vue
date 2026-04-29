<!--
  多标签页导航
  - el-tag 列表，点击切换页面
  - 关闭单个标签（affix 不可关闭）
  - 右键菜单：关闭当前、关闭其他、关闭全部
-->
<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTagsViewStore } from '@/store/modules/tags-view'
import type { TagView } from '@/types'

const route = useRoute()
const router = useRouter()
const tagsViewStore = useTagsViewStore()

// 右键菜单状态
const contextMenuVisible = ref(false)
const contextMenuStyle = ref({ left: '0px', top: '0px' })
const selectedTag = ref<TagView | null>(null)

/** 点击标签切换页面 */
function handleTagClick(view: TagView) {
  router.push(view.fullPath || view.path)
}

/** 关闭标签 */
function handleTagClose(view: TagView) {
  const nextView = tagsViewStore.closeView(view)
  // 如果关闭的是当前标签，跳转到下一个标签
  if (view.path === route.path && nextView) {
    router.push(nextView.fullPath || nextView.path)
  } else if (tagsViewStore.visitedViews.length === 0) {
    router.push('/home')
  }
}

/** 右键菜单 */
function handleContextMenu(e: MouseEvent, view: TagView) {
  e.preventDefault()
  selectedTag.value = view
  contextMenuStyle.value = {
    left: e.clientX + 'px',
    top: e.clientY + 'px'
  }
  contextMenuVisible.value = true
}

/** 关闭右键菜单 */
function closeContextMenu() {
  contextMenuVisible.value = false
}

/** 关闭当前标签 */
function closeCurrentTag() {
  if (selectedTag.value) {
    handleTagClose(selectedTag.value)
  }
  closeContextMenu()
}

/** 关闭其他标签 */
function closeOtherTags() {
  if (selectedTag.value) {
    tagsViewStore.closeOtherViews(selectedTag.value)
    router.push(selectedTag.value.fullPath || selectedTag.value.path)
  }
  closeContextMenu()
}

/** 关闭全部标签 */
function closeAllTags() {
  tagsViewStore.closeAllViews()
  router.push('/home')
  closeContextMenu()
}

// 点击其他区域关闭右键菜单
watch(contextMenuVisible, (val) => {
  if (val) {
    document.addEventListener('click', closeContextMenu)
  } else {
    document.removeEventListener('click', closeContextMenu)
  }
})
</script>

<template>
  <div class="tags-view">
    <div class="tags-view__wrapper">
      <el-tag
        v-for="tag in tagsViewStore.visitedViews"
        :key="tag.path"
        :closable="!tag.affix"
        :effect="tag.path === route.path ? 'dark' : 'plain'"
        :color="tag.path === route.path ? '#1890FF' : undefined"
        class="tags-view__item"
        @click="handleTagClick(tag)"
        @close="handleTagClose(tag)"
        @contextmenu="handleContextMenu($event, tag)"
      >
        {{ tag.title }}
      </el-tag>
    </div>

    <!-- 右键菜单 -->
    <ul
      v-show="contextMenuVisible"
      class="tags-view__context-menu"
      :style="contextMenuStyle"
    >
      <li @click="closeCurrentTag">关闭当前</li>
      <li @click="closeOtherTags">关闭其他</li>
      <li @click="closeAllTags">关闭全部</li>
    </ul>
  </div>
</template>

<style scoped>
.tags-view {
  background-color: var(--color-bg-white);
  border-bottom: 1px solid var(--color-border);
  padding: var(--spacing-sm) var(--spacing-xl);
}

.tags-view__wrapper {
  display: flex;
  flex-wrap: nowrap;
  overflow-x: auto;
  gap: var(--spacing-xs);
}

.tags-view__wrapper::-webkit-scrollbar {
  height: 4px;
}

.tags-view__item {
  cursor: pointer;
  flex-shrink: 0;
}

.tags-view__context-menu {
  position: fixed;
  z-index: 3000;
  list-style: none;
  padding: 4px 0;
  margin: 0;
  background: var(--color-bg-white);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-sm);
  box-shadow: var(--shadow-md);
}

.tags-view__context-menu li {
  padding: 6px 16px;
  font-size: 13px;
  color: var(--color-text-regular);
  cursor: pointer;
}

.tags-view__context-menu li:hover {
  background-color: var(--color-bg-page);
  color: var(--color-primary);
}
</style>
