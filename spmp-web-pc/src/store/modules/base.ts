/**
 * 基础数据 Store — 级联选择器数据缓存与联动逻辑
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCascadeData } from '@/api/base/cascade'
import type { CascadeItem } from '@/api/base/cascade'

export const useBaseStore = defineStore('base', () => {
  /** 片区列表缓存 */
  const districtList = ref<CascadeItem[]>([])
  /** 小区列表缓存（按片区 ID 索引） */
  const communityMap = ref<Record<number, CascadeItem[]>>({})
  /** 楼栋列表缓存（按小区 ID 索引） */
  const buildingMap = ref<Record<number, CascadeItem[]>>({})
  /** 单元列表缓存（按楼栋 ID 索引） */
  const unitMap = ref<Record<number, CascadeItem[]>>({})

  /** 加载片区列表 */
  async function loadDistricts() {
    if (districtList.value.length) return districtList.value
    districtList.value = await getCascadeData('DISTRICT')
    return districtList.value
  }

  /** 加载指定片区下的小区列表 */
  async function loadCommunities(districtId: number) {
    if (communityMap.value[districtId]) return communityMap.value[districtId]
    const list = await getCascadeData('COMMUNITY', districtId)
    communityMap.value[districtId] = list
    return list
  }

  /** 加载指定小区下的楼栋列表 */
  async function loadBuildings(communityId: number) {
    if (buildingMap.value[communityId]) return buildingMap.value[communityId]
    const list = await getCascadeData('BUILDING', communityId)
    buildingMap.value[communityId] = list
    return list
  }

  /** 加载指定楼栋下的单元列表 */
  async function loadUnits(buildingId: number) {
    if (unitMap.value[buildingId]) return unitMap.value[buildingId]
    const list = await getCascadeData('UNIT', buildingId)
    unitMap.value[buildingId] = list
    return list
  }

  /** 清除所有缓存（数据变更后调用） */
  function clearCache() {
    districtList.value = []
    communityMap.value = {}
    buildingMap.value = {}
    unitMap.value = {}
  }

  return {
    districtList,
    communityMap,
    buildingMap,
    unitMap,
    loadDistricts,
    loadCommunities,
    loadBuildings,
    loadUnits,
    clearCache
  }
})
