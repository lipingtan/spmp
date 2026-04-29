<template>
  <el-row :gutter="8" style="width: 100%">
    <el-col :span="colSpan" v-if="showDistrict">
      <el-select v-model="selectedDistrict" placeholder="选择片区" clearable filterable @change="onDistrictChange" style="width: 100%">
        <el-option v-for="d in districtList" :key="d.id" :label="d.name" :value="d.id" />
      </el-select>
    </el-col>
    <el-col :span="colSpan" v-if="showCommunity">
      <el-select v-model="selectedCommunity" placeholder="选择小区" clearable filterable :disabled="showDistrict && !selectedDistrict" @change="onCommunityChange" style="width: 100%">
        <el-option v-for="c in communityList" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
    </el-col>
    <el-col :span="colSpan" v-if="showBuilding">
      <el-select v-model="selectedBuilding" placeholder="选择楼栋" clearable filterable :disabled="!selectedCommunity" @change="onBuildingChange" style="width: 100%">
        <el-option v-for="b in buildingList" :key="b.id" :label="b.name" :value="b.id" />
      </el-select>
    </el-col>
    <el-col :span="colSpan" v-if="showUnit">
      <el-select v-model="selectedUnit" placeholder="选择单元" clearable filterable :disabled="!selectedBuilding" @change="onUnitChange" style="width: 100%">
        <el-option v-for="u in unitList" :key="u.id" :label="u.name" :value="u.id" />
      </el-select>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import type { CascadeItem } from '@/api/base/cascade'
import { useBaseStore } from '@/store/modules/base'

export interface CascadeValue {
  districtId?: number
  communityId?: number
  buildingId?: number
  unitId?: number
}

const props = withDefaults(defineProps<{
  /** 显示层级：district / community / building / unit */
  levels?: ('district' | 'community' | 'building' | 'unit')[]
  modelValue?: CascadeValue
}>(), {
  levels: () => ['community', 'building', 'unit']
})

const emit = defineEmits<{
  'update:modelValue': [value: CascadeValue]
  change: [value: CascadeValue]
}>()

const baseStore = useBaseStore()

const showDistrict = computed(() => props.levels.includes('district'))
const showCommunity = computed(() => props.levels.includes('community'))
const showBuilding = computed(() => props.levels.includes('building'))
const showUnit = computed(() => props.levels.includes('unit'))
const colSpan = computed(() => Math.floor(24 / props.levels.length))

const selectedDistrict = ref<number>()
const selectedCommunity = ref<number>()
const selectedBuilding = ref<number>()
const selectedUnit = ref<number>()

const districtList = ref<CascadeItem[]>([])
const communityList = ref<CascadeItem[]>([])
const buildingList = ref<CascadeItem[]>([])
const unitList = ref<CascadeItem[]>([])

function emitValue() {
  const val: CascadeValue = {
    districtId: selectedDistrict.value,
    communityId: selectedCommunity.value,
    buildingId: selectedBuilding.value,
    unitId: selectedUnit.value
  }
  emit('update:modelValue', val)
  emit('change', val)
}

async function onDistrictChange(val: number | undefined) {
  selectedCommunity.value = undefined
  selectedBuilding.value = undefined
  selectedUnit.value = undefined
  communityList.value = []
  buildingList.value = []
  unitList.value = []
  if (val) communityList.value = await baseStore.loadCommunities(val)
  emitValue()
}

async function onCommunityChange(val: number | undefined) {
  selectedBuilding.value = undefined
  selectedUnit.value = undefined
  buildingList.value = []
  unitList.value = []
  if (val) buildingList.value = await baseStore.loadBuildings(val)
  emitValue()
}

async function onBuildingChange(val: number | undefined) {
  selectedUnit.value = undefined
  unitList.value = []
  if (val) unitList.value = await baseStore.loadUnits(val)
  emitValue()
}

function onUnitChange() {
  emitValue()
}

/** 重置所有选择 */
function reset() {
  selectedDistrict.value = undefined
  selectedCommunity.value = undefined
  selectedBuilding.value = undefined
  selectedUnit.value = undefined
  communityList.value = []
  buildingList.value = []
  unitList.value = []
}

onMounted(async () => {
  if (showDistrict.value) {
    districtList.value = await baseStore.loadDistricts()
  }
  if (showCommunity.value && !showDistrict.value) {
    // 不显示片区时，直接加载所有小区
    const { getCascadeData } = await import('@/api/base/cascade')
    communityList.value = await getCascadeData('COMMUNITY')
  }
})

defineExpose({ reset })
</script>
