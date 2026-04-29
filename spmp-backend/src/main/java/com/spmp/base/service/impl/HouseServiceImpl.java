package com.spmp.base.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.base.constant.BaseConstants;
import com.spmp.base.constant.BaseErrorCode;
import com.spmp.base.constant.HouseStatus;
import com.spmp.base.constant.HouseType;
import com.spmp.base.domain.dto.HouseCreateDTO;
import com.spmp.base.domain.dto.HousePageDTO;
import com.spmp.base.domain.dto.HouseQueryDTO;
import com.spmp.base.domain.dto.HouseStatusChangeDTO;
import com.spmp.base.domain.dto.HouseUpdateDTO;
import com.spmp.base.domain.dto.ImportResultDTO;
import com.spmp.base.domain.entity.BuildingDO;
import com.spmp.base.domain.entity.HouseDO;
import com.spmp.base.domain.entity.HouseStatusLogDO;
import com.spmp.base.domain.entity.UnitDO;
import com.spmp.base.excel.ExcelImportHelper;
import com.spmp.base.excel.HouseExcelListener;
import com.spmp.base.excel.HouseExcelModel;
import com.spmp.base.repository.BuildingMapper;
import com.spmp.base.repository.CommunityMapper;
import com.spmp.base.repository.HouseMapper;
import com.spmp.base.repository.HouseStatusLogMapper;
import com.spmp.base.repository.UnitMapper;
import com.spmp.base.service.BaseCacheService;
import com.spmp.base.service.HouseService;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.result.PageResult;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * 房屋管理服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class HouseServiceImpl implements HouseService {

    private final HouseMapper houseMapper;
    private final UnitMapper unitMapper;
    private final BuildingMapper buildingMapper;
    private final CommunityMapper communityMapper;
    private final HouseStatusLogMapper houseStatusLogMapper;
    private final BaseCacheService baseCacheService;

    public HouseServiceImpl(HouseMapper houseMapper,
                            UnitMapper unitMapper,
                            BuildingMapper buildingMapper,
                            CommunityMapper communityMapper,
                            HouseStatusLogMapper houseStatusLogMapper,
                            @Lazy BaseCacheService baseCacheService) {
        this.houseMapper = houseMapper;
        this.unitMapper = unitMapper;
        this.buildingMapper = buildingMapper;
        this.communityMapper = communityMapper;
        this.houseStatusLogMapper = houseStatusLogMapper;
        this.baseCacheService = baseCacheService;
    }

    @Override
    public PageResult<HousePageDTO> listHouses(HouseQueryDTO queryDTO) {
        IPage<HousePageDTO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<HousePageDTO> result = houseMapper.selectHousePage(page, queryDTO);
        return PageResult.of(result);
    }

    @Override
    public void createHouse(HouseCreateDTO createDTO) {
        // 校验房屋状态枚举值
        if (!HouseStatus.isValid(createDTO.getHouseStatus())) {
            throw new BusinessException(BaseErrorCode.HOUSE_STATUS_INVALID.getCode(),
                    BaseErrorCode.HOUSE_STATUS_INVALID.getMessage());
        }

        // 校验房屋类型枚举值
        if (!HouseType.isValid(createDTO.getHouseType())) {
            throw new BusinessException(BaseErrorCode.HOUSE_TYPE_INVALID.getCode(),
                    BaseErrorCode.HOUSE_TYPE_INVALID.getMessage());
        }

        // 校验所属单元启用状态
        UnitDO unit = unitMapper.selectById(createDTO.getUnitId());
        if (unit == null) {
            throw new BusinessException(BaseErrorCode.UNIT_NOT_FOUND.getCode(),
                    BaseErrorCode.UNIT_NOT_FOUND.getMessage());
        }
        if (BaseConstants.STATUS_DISABLED == unit.getStatus()) {
            throw new BusinessException(BaseErrorCode.UNIT_DISABLED.getCode(),
                    BaseErrorCode.UNIT_DISABLED.getMessage());
        }

        // 编号在同一单元内唯一校验
        LambdaQueryWrapper<HouseDO> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(HouseDO::getHouseCode, createDTO.getHouseCode())
                .eq(HouseDO::getUnitId, createDTO.getUnitId());
        if (houseMapper.selectCount(codeWrapper) > 0) {
            throw new BusinessException(BaseErrorCode.HOUSE_CODE_EXISTS.getCode(),
                    BaseErrorCode.HOUSE_CODE_EXISTS.getMessage());
        }

        HouseDO house = new HouseDO();
        house.setHouseCode(createDTO.getHouseCode());
        house.setUnitId(createDTO.getUnitId());
        house.setFloor(createDTO.getFloor());
        house.setBuildingArea(createDTO.getBuildingArea());
        house.setUsableArea(createDTO.getUsableArea());
        house.setHouseStatus(createDTO.getHouseStatus());
        house.setHouseType(createDTO.getHouseType());
        house.setRemark(createDTO.getRemark());
        houseMapper.insert(house);

        // 记录初始状态到 bs_house_status_log
        HouseStatusLogDO statusLog = new HouseStatusLogDO();
        statusLog.setHouseId(house.getId());
        statusLog.setOldStatus(null);
        statusLog.setNewStatus(createDTO.getHouseStatus());
        statusLog.setChangeTime(new Date());
        statusLog.setOperatorId(getCurrentUserId());
        houseStatusLogMapper.insert(statusLog);

        evictHouseCacheInternal(house.getId(), createDTO.getUnitId());
        log.info("新增房屋成功, id={}, code={}", house.getId(), house.getHouseCode());
    }

    @Override
    public void updateHouse(Long id, HouseUpdateDTO updateDTO) {
        HouseDO house = houseMapper.selectById(id);
        if (house == null) {
            throw new BusinessException(BaseErrorCode.HOUSE_NOT_FOUND.getCode(),
                    BaseErrorCode.HOUSE_NOT_FOUND.getMessage());
        }

        // 校验房屋类型枚举值（如果传了）
        if (updateDTO.getHouseType() != null && !HouseType.isValid(updateDTO.getHouseType())) {
            throw new BusinessException(BaseErrorCode.HOUSE_TYPE_INVALID.getCode(),
                    BaseErrorCode.HOUSE_TYPE_INVALID.getMessage());
        }

        // 编号唯一校验（排除自身，同一单元内）
        LambdaQueryWrapper<HouseDO> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(HouseDO::getHouseCode, updateDTO.getHouseCode())
                .eq(HouseDO::getUnitId, house.getUnitId())
                .ne(HouseDO::getId, id);
        if (houseMapper.selectCount(codeWrapper) > 0) {
            throw new BusinessException(BaseErrorCode.HOUSE_CODE_EXISTS.getCode(),
                    BaseErrorCode.HOUSE_CODE_EXISTS.getMessage());
        }

        // 禁止修改所属单元 — 只更新允许的字段
        house.setHouseCode(updateDTO.getHouseCode());
        if (updateDTO.getFloor() != null) {
            house.setFloor(updateDTO.getFloor());
        }
        if (updateDTO.getBuildingArea() != null) {
            house.setBuildingArea(updateDTO.getBuildingArea());
        }
        if (updateDTO.getUsableArea() != null) {
            house.setUsableArea(updateDTO.getUsableArea());
        }
        if (updateDTO.getHouseType() != null) {
            house.setHouseType(updateDTO.getHouseType());
        }
        house.setRemark(updateDTO.getRemark());
        houseMapper.updateById(house);

        evictHouseCacheInternal(id, house.getUnitId());
        log.info("编辑房屋成功, id={}", id);
    }

    @Override
    public void deleteHouse(Long id) {
        HouseDO house = houseMapper.selectById(id);
        if (house == null) {
            throw new BusinessException(BaseErrorCode.HOUSE_NOT_FOUND.getCode(),
                    BaseErrorCode.HOUSE_NOT_FOUND.getMessage());
        }

        // 逻辑删除
        houseMapper.deleteById(id);

        evictHouseCacheInternal(id, house.getUnitId());
        log.info("删除房屋成功, id={}", id);
    }

    @Override
    public void changeHouseStatus(Long id, HouseStatusChangeDTO statusDTO) {
        // 校验房屋状态枚举值
        if (!HouseStatus.isValid(statusDTO.getHouseStatus())) {
            throw new BusinessException(BaseErrorCode.HOUSE_STATUS_INVALID.getCode(),
                    BaseErrorCode.HOUSE_STATUS_INVALID.getMessage());
        }

        HouseDO house = houseMapper.selectById(id);
        if (house == null) {
            throw new BusinessException(BaseErrorCode.HOUSE_NOT_FOUND.getCode(),
                    BaseErrorCode.HOUSE_NOT_FOUND.getMessage());
        }

        String oldStatus = house.getHouseStatus();

        // 更新房屋状态
        house.setHouseStatus(statusDTO.getHouseStatus());
        houseMapper.updateById(house);

        // 记录变更历史到 bs_house_status_log
        HouseStatusLogDO statusLog = new HouseStatusLogDO();
        statusLog.setHouseId(id);
        statusLog.setOldStatus(oldStatus);
        statusLog.setNewStatus(statusDTO.getHouseStatus());
        statusLog.setChangeTime(new Date());
        statusLog.setOperatorId(getCurrentUserId());
        houseStatusLogMapper.insert(statusLog);

        evictHouseCacheInternal(id, house.getUnitId());
        log.info("房屋状态变更成功, id={}, oldStatus={}, newStatus={}", id, oldStatus, statusDTO.getHouseStatus());
    }

    @Override
    public List<HouseStatusLogDO> getStatusLogs(Long id) {
        LambdaQueryWrapper<HouseStatusLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseStatusLogDO::getHouseId, id)
                .orderByDesc(HouseStatusLogDO::getChangeTime);
        return houseStatusLogMapper.selectList(wrapper);
    }

    @Override
    public ImportResultDTO importHouses(MultipartFile file) {
        ExcelImportHelper.validateFile(file);
        try {
            HouseExcelListener listener = new HouseExcelListener(houseMapper, unitMapper, buildingMapper, communityMapper);
            EasyExcel.read(file.getInputStream(), HouseExcelModel.class, listener)
                    .sheet().headRowNumber(1).doRead();

            if (listener.getSuccessCount() == 0 && listener.getErrorList().isEmpty()) {
                throw new BusinessException(BaseErrorCode.IMPORT_DATA_EMPTY.getCode(),
                        BaseErrorCode.IMPORT_DATA_EMPTY.getMessage());
            }
            return ExcelImportHelper.buildResult(listener.getSuccessCount(), listener.getErrorList(), "房屋");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("房屋导入失败: {}", e.getMessage(), e);
            throw new BusinessException(BaseErrorCode.IMPORT_FILE_FORMAT_ERROR.getCode(), "导入失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) {
        ExcelImportHelper.downloadTemplate(response, HouseExcelModel.class, "房屋导入模板", "房屋导入模板.xlsx");
    }

    /**
     * 清除房屋相关缓存（内部辅助方法）。
     *
     * @param houseId 房屋ID
     * @param unitId  所属单元ID
     */
    private void evictHouseCacheInternal(Long houseId, Long unitId) {
        try {
            UnitDO unit = unitMapper.selectById(unitId);
            Long buildingId = unit != null ? unit.getBuildingId() : null;
            Long communityId = null;
            if (buildingId != null) {
                BuildingDO building = buildingMapper.selectById(buildingId);
                communityId = building != null ? building.getCommunityId() : null;
            }
            baseCacheService.evictHouseCache(houseId, unitId, communityId);
        } catch (Exception e) {
            log.warn("清除房屋缓存失败, houseId={}: {}", houseId, e.getMessage());
        }
    }

    /**
     * 获取当前登录用户ID。
     * <p>
     * 从 SecurityContext 中的 JWT Claims 提取 userId。
     *
     * @return 当前用户ID，未登录时返回 null
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getDetails() instanceof Claims) {
                Claims claims = (Claims) authentication.getDetails();
                Object userId = claims.get("userId");
                if (userId instanceof Number) {
                    return ((Number) userId).longValue();
                }
            }
        } catch (Exception e) {
            log.warn("获取当前用户ID失败: {}", e.getMessage());
        }
        return null;
    }
}
