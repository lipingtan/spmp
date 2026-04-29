<script setup lang="ts">
defineProps<{
  title: string
  permission: string
  route: string
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'go', route: string): void
}>()
</script>

<template>
  <el-button
    v-if="visible"
    class="quick-entry"
    type="primary"
    plain
    @click="emit('go', route)"
  >
    {{ title }}
  </el-button>
</template>

<style scoped>
.quick-entry {
  margin-right: 12px;
  margin-bottom: 12px;
}
</style>

