package com.spmp.owner.service.impl;

import com.spmp.common.util.RedisUtils;
import com.spmp.owner.api.dto.OwnerBriefDTO;
import com.spmp.owner.constant.OwnerConstants;
import com.spmp.owner.service.OwnerCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 业主缓存管理服务实现类。
 * <p>
 * 降级方案：所有 Redis 操作均 try-catch，异常时 log.warn 后降级，不影响业务流程。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OwnerCacheServiceImpl implements OwnerCacheService {

    private final RedisUtils redisUtils;

    // ========== 按 ID 查业主缓存 ==========

    @Override
    public OwnerBriefDTO getOwnerById(Long ownerId) {
        try {
            return redisUtils.get(OwnerConstants.CACHE_OWNER_ID_KEY + ownerId, OwnerBriefDTO.class);
        } catch (Exception e) {
            log.warn("获取业主缓存失败，降级查数据库，ownerId={}: {}", ownerId, e.getMessage());
            return null;
        }
    }

    @Override
    public void setOwnerById(Long ownerId, OwnerBriefDTO dto) {
        try {
            redisUtils.set(OwnerConstants.CACHE_OWNER_ID_KEY + ownerId, dto,
                    OwnerConstants.CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("写入业主缓存失败，ownerId={}: {}", ownerId, e.getMessage());
        }
    }

    // ========== 按房屋查业主缓存 ==========

    @Override
    public OwnerBriefDTO getOwnerByHouseId(Long houseId) {
        try {
            return redisUtils.get(OwnerConstants.CACHE_OWNER_HOUSE_KEY + houseId, OwnerBriefDTO.class);
        } catch (Exception e) {
            log.warn("获取房屋业主缓存失败，降级查数据库，houseId={}: {}", houseId, e.getMessage());
            return null;
        }
    }

    @Override
    public void setOwnerByHouseId(Long houseId, OwnerBriefDTO dto) {
        try {
            redisUtils.set(OwnerConstants.CACHE_OWNER_HOUSE_KEY + houseId, dto,
                    OwnerConstants.CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("写入房屋业主缓存失败，houseId={}: {}", houseId, e.getMessage());
        }
    }

    // ========== 按手机号查业主缓存 ==========

    @Override
    public OwnerBriefDTO getOwnerByPhone(String encryptedPhone) {
        try {
            return redisUtils.get(OwnerConstants.CACHE_OWNER_PHONE_KEY + encryptedPhone, OwnerBriefDTO.class);
        } catch (Exception e) {
            log.warn("获取手机号业主缓存失败，降级查数据库: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void setOwnerByPhone(String encryptedPhone, OwnerBriefDTO dto) {
        try {
            redisUtils.set(OwnerConstants.CACHE_OWNER_PHONE_KEY + encryptedPhone, dto,
                    OwnerConstants.CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("写入手机号业主缓存失败: {}", e.getMessage());
        }
    }

    // ========== 按小区查业主列表缓存 ==========

    @Override
    @SuppressWarnings("unchecked")
    public List<OwnerBriefDTO> getOwnersByCommunityId(Long communityId) {
        try {
            return redisUtils.get(OwnerConstants.CACHE_OWNER_COMMUNITY_KEY + communityId, List.class);
        } catch (Exception e) {
            log.warn("获取小区业主列表缓存失败，降级查数据库，communityId={}: {}", communityId, e.getMessage());
            return null;
        }
    }

    @Override
    public void setOwnersByCommunityId(Long communityId, List<OwnerBriefDTO> list) {
        try {
            redisUtils.set(OwnerConstants.CACHE_OWNER_COMMUNITY_KEY + communityId, list,
                    OwnerConstants.CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("写入小区业主列表缓存失败，communityId={}: {}", communityId, e.getMessage());
        }
    }

    // ========== 按楼栋查业主列表缓存 ==========

    @Override
    @SuppressWarnings("unchecked")
    public List<OwnerBriefDTO> getOwnersByBuildingId(Long buildingId) {
        try {
            return redisUtils.get(OwnerConstants.CACHE_OWNER_BUILDING_KEY + buildingId, List.class);
        } catch (Exception e) {
            log.warn("获取楼栋业主列表缓存失败，降级查数据库，buildingId={}: {}", buildingId, e.getMessage());
            return null;
        }
    }

    @Override
    public void setOwnersByBuildingId(Long buildingId, List<OwnerBriefDTO> list) {
        try {
            redisUtils.set(OwnerConstants.CACHE_OWNER_BUILDING_KEY + buildingId, list,
                    OwnerConstants.CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("写入楼栋业主列表缓存失败，buildingId={}: {}", buildingId, e.getMessage());
        }
    }

    // ========== 业主认证状态缓存 ==========

    @Override
    public Boolean getOwnerCertified(Long ownerId) {
        try {
            return redisUtils.get(OwnerConstants.CACHE_OWNER_CERTIFIED_KEY + ownerId, Boolean.class);
        } catch (Exception e) {
            log.warn("获取业主认证状态缓存失败，降级查数据库，ownerId={}: {}", ownerId, e.getMessage());
            return null;
        }
    }

    @Override
    public void setOwnerCertified(Long ownerId, boolean certified) {
        try {
            redisUtils.set(OwnerConstants.CACHE_OWNER_CERTIFIED_KEY + ownerId, certified,
                    OwnerConstants.CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("写入业主认证状态缓存失败，ownerId={}: {}", ownerId, e.getMessage());
        }
    }

    // ========== 缓存清除 ==========

    @Override
    public void clearOwnerCache(Long ownerId) {
        try {
            redisUtils.delete(OwnerConstants.CACHE_OWNER_ID_KEY + ownerId);
            redisUtils.delete(OwnerConstants.CACHE_OWNER_CERTIFIED_KEY + ownerId);
        } catch (Exception e) {
            log.warn("清除业主缓存失败，ownerId={}: {}", ownerId, e.getMessage());
        }
    }

    @Override
    public void clearHouseCache(Long houseId) {
        try {
            redisUtils.delete(OwnerConstants.CACHE_OWNER_HOUSE_KEY + houseId);
        } catch (Exception e) {
            log.warn("清除房屋缓存失败，houseId={}: {}", houseId, e.getMessage());
        }
    }

    @Override
    public void clearPhoneCache(String encryptedPhone) {
        try {
            redisUtils.delete(OwnerConstants.CACHE_OWNER_PHONE_KEY + encryptedPhone);
        } catch (Exception e) {
            log.warn("清除手机号缓存失败: {}", e.getMessage());
        }
    }

    @Override
    public void clearCommunityCache(Long communityId) {
        try {
            redisUtils.delete(OwnerConstants.CACHE_OWNER_COMMUNITY_KEY + communityId);
        } catch (Exception e) {
            log.warn("清除小区缓存失败，communityId={}: {}", communityId, e.getMessage());
        }
    }

    @Override
    public void clearBuildingCache(Long buildingId) {
        try {
            redisUtils.delete(OwnerConstants.CACHE_OWNER_BUILDING_KEY + buildingId);
        } catch (Exception e) {
            log.warn("清除楼栋缓存失败，buildingId={}: {}", buildingId, e.getMessage());
        }
    }
}
