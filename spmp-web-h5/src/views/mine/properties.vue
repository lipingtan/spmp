<!--
  H5 我的房产页面
  - 展示已绑定的房产列表（小区、楼栋、单元、房屋编号、关系类型）
  - 数据通过 getProfile API 获取，从 propertyBindings 字段提取
  - 空状态使用 van-empty
-->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getProfile } from '@/api/owner'
import type { PropertyBindingItem } from '@/api/owner'

// ==================== 状态 ====================

/** 房产绑定列表 */
const propertyList = ref<PropertyBindingItem[]>([])
/** 加载状态 */
const loading = ref(false)

// ==================== 关系类型映射 ====================

/** 关系类型文本映射 */
function relationText(type: string): string {
  const map: Record<string, string> = {
    OWNER: '业主',
    TENANT: '租户',
    FAMILY: '家属'
  }
  return map[type] ?? type
}

/** 关系类型标签颜色映射 */
function relationTagType(type: string): 'primary' | 'warning' | 'success' {
  const map: Record<string, 'primary' | 'warning' | 'success'> = {
    OWNER: 'primary',
    TENANT: 'warning',
    FAMILY: 'success'
  }
  return map[type] ?? 'primary'
}

// ==================== 数据加载 ====================

/** 加载房产列表 */
async function fetchProperties() {
  loading.value = true
  try {
    const profile = await getProfile()
    propertyList.value = profile.propertyBindings ?? []
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchProperties()
})
</script>

<template>
  <div class="properties-page">
    <!-- 顶部导航栏 -->
    <van-nav-bar title="我的房产" left-arrow @click-left="$router.back()" />

    <!-- 加载状态 -->
    <div v-if="loading" class="page-loading">
      <van-loading size="24px">加载中...</van-loading>
    </div>

    <!-- 房产列表 -->
    <template v-else>
      <van-empty v-if="!propertyList.length" description="暂无绑定房产" />
      <div v-else class="property-list">
        <van-cell-group
          inset
          v-for="item in propertyList"
          :key="item.id"
          class="property-item"
        >
          <van-cell :border="false">
            <template #title>
              <div class="property-address">
                {{ item.communityName }} {{ item.buildingName }} {{ item.unitName }} {{ item.houseName }}
              </div>
            </template>
            <template #right-icon>
              <van-tag :type="relationTagType(item.relationType)" size="medium">
                {{ relationText(item.relationType) }}
              </van-tag>
            </template>
          </van-cell>
        </van-cell-group>
      </div>
    </template>
  </div>
</template>

<style scoped>
.properties-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--color-bg-page);
}

.page-loading {
  display: flex;
  justify-content: center;
  padding: var(--spacing-xxl) 0;
}

.property-list {
  padding: var(--spacing-lg) 0;
}

.property-item {
  margin-bottom: var(--spacing-sm);
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.property-address {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.5;
  letter-spacing: 0.2px;
  text-shadow: 0 1px 1px rgba(0, 0, 0, 0.08);
}

.property-list :deep(.van-cell) {
  background: var(--color-surface);
}

.property-list :deep(.van-cell__title) {
  color: var(--color-text-primary);
}

.property-list :deep(.van-tag) {
  font-weight: 700;
  letter-spacing: 0.2px;
}

.properties-page :deep(.van-empty__description) {
  color: var(--color-text-regular);
  font-weight: 600;
}
</style>
