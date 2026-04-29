package com.spmp.base.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.base.constant.BaseConstants;
import com.spmp.base.constant.BaseErrorCode;
import com.spmp.base.constant.BuildingType;
import com.spmp.base.domain.dto.BuildingCreateDTO;
import com.spmp.base.domain.dto.BuildingPageDTO;
import com.spmp.base.domain.dto.BuildingQueryDTO;
import com.spmp.base.domain.dto.BuildingUpdateDTO;
import com.spmp.base.domain.dto.ImportResultDTO;
import com.spmp.base.domain.dto.StatusChangeDTO;
import com.spmp.base.domain.entity.BuildingDO;
import com.spmp.base.domain.entity.CommunityDO;
import com.spmp.base.domain.entity.UnitDO;
import com.spmp.base.excel.BuildingExcelListener;
import com.spmp.base.excel.BuildingExcelModel;
import com.spmp.base.excel.ExcelImportHelper;
import com.spmp.base.repository.BuildingMapper;
import com.spmp.base.repository.CommunityMapper;
import com.spmp.base.repository.UnitMapper;
import com.spmp.base.service.BaseCacheService;
import com.spmp.base.service.BuildingService;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 楼栋管理服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class BuildingServiceImpl implements BuildingService {

    private final BuildingMapper buildingMapper;
    private final CommunityMapper communityMapper;
    private final UnitMapper unitMapper;
    private final BaseCacheService baseCacheService;

    public BuildingServiceImpl(BuildingMapper buildingMapper,
                               CommunityMapper communityMapper,
                               UnitMapper unitMapper,
                               @Lazy BaseCacheService baseCacheService) {
        this.buildingMapper = buildingMapper;
        this.communityMapper = communityMapper;
        this.unitMapper = unitMapper;
        this.baseCacheService = baseCacheService;
    }

    @Override
    public PageResult<BuildingPageDTO> listBuildings(BuildingQueryDTO queryDTO) {
        IPage<BuildingPageDTO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<BuildingPageDTO> result = buildingMapper.selectBuildingPage(page, queryDTO);
        return PageResult.of(result);
    }

    @Override
    public void createBuilding(BuildingCreateDTO createDTO) {
        // 校验楼栋类型枚举值
        if (!BuildingType.isValid(createDTO.getBuildingType())) {
            throw new BusinessException(BaseErrorCode.BUILDING_TYPE_INVALID.getCode(),
                    BaseErrorCode.BUILDING_TYPE_INVALID.getMessage());
        }

        // 校验所属小区启用状态
        CommunityDO community = communityMapper.selectById(createDTO.getCommunityId());
        if (community == null) {
            throw new BusinessException(BaseErrorCode.COMMUNITY_NOT_FOUND.getCode(),
                    BaseErrorCode.COMMUNITY_NOT_FOUND.getMessage());
        }
        if (BaseConstants.STATUS_DISABLED == community.getStatus()) {
            throw new BusinessException(BaseErrorCode.COMMUNITY_DISABLED.getCode(),
                    BaseErrorCode.COMMUNITY_DISABLED.getMessage());
        }

        // 编号在同一小区内唯一校验
        LambdaQueryWrapper<BuildingDO> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(BuildingDO::getBuildingCode, createDTO.getBuildingCode())
                .eq(BuildingDO::getCommunityId, createDTO.getCommunityId());
        if (buildingMapper.selectCount(codeWrapper) > 0) {
            throw new BusinessException(BaseErrorCode.BUILDING_CODE_EXISTS.getCode(),
                    BaseErrorCode.BUILDING_CODE_EXISTS.getMessage());
        }

        BuildingDO building = new BuildingDO();
        building.setBuildingName(createDTO.getBuildingName());
        building.setBuildingCode(createDTO.getBuildingCode());
        building.setCommunityId(createDTO.getCommunityId());
        building.setAboveGroundFloors(createDTO.getAboveGroundFloors());
        building.setUndergroundFloors(createDTO.getUndergroundFloors());
        building.setUnitsPerFloor(createDTO.getUnitsPerFloor());
        building.setBuildingType(createDTO.getBuildingType());
        building.setStatus(BaseConstants.STATUS_ENABLED);
        building.setRemark(createDTO.getRemark());
        buildingMapper.insert(building);

        baseCacheService.evictBuildingCache(building.getId(), createDTO.getCommunityId());
        log.info("新增楼栋成功, id={}, name={}, code={}", building.getId(), building.getBuildingName(), building.getBuildingCode());
    }

    @Override
    public void updateBuilding(Long id, BuildingUpdateDTO updateDTO) {
        BuildingDO building = buildingMapper.selectById(id);
        if (building == null) {
            throw new BusinessException(BaseErrorCode.BUILDING_NOT_FOUND.getCode(),
                    BaseErrorCode.BUILDING_NOT_FOUND.getMessage());
        }

        // 校验楼栋类型枚举值（如果传了）
        if (updateDTO.getBuildingType() != null && !BuildingType.isValid(updateDTO.getBuildingType())) {
            throw new BusinessException(BaseErrorCode.BUILDING_TYPE_INVALID.getCode(),
                    BaseErrorCode.BUILDING_TYPE_INVALID.getMessage());
        }

        // 编号唯一校验（排除自身，同一小区内）
        LambdaQueryWrapper<BuildingDO> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(BuildingDO::getBuildingCode, updateDTO.getBuildingCode())
                .eq(BuildingDO::getCommunityId, building.getCommunityId())
                .ne(BuildingDO::getId, id);
        if (buildingMapper.selectCount(codeWrapper) > 0) {
            throw new BusinessException(BaseErrorCode.BUILDING_CODE_EXISTS.getCode(),
                    BaseErrorCode.BUILDING_CODE_EXISTS.getMessage());
        }

        // 禁止修改所属小区 — 只更新允许的字段
        building.setBuildingName(updateDTO.getBuildingName());
        building.setBuildingCode(updateDTO.getBuildingCode());
        if (updateDTO.getAboveGroundFloors() != null) {
            building.setAboveGroundFloors(updateDTO.getAboveGroundFloors());
        }
        if (updateDTO.getUndergroundFloors() != null) {
            building.setUndergroundFloors(updateDTO.getUndergroundFloors());
        }
        if (updateDTO.getUnitsPerFloor() != null) {
            building.setUnitsPerFloor(updateDTO.getUnitsPerFloor());
        }
        if (updateDTO.getBuildingType() != null) {
            building.setBuildingType(updateDTO.getBuildingType());
        }
        building.setRemark(updateDTO.getRemark());
        buildingMapper.updateById(building);

        baseCacheService.evictBuildingCache(id, building.getCommunityId());
        log.info("编辑楼栋成功, id={}", id);
    }

    @Override
    public void deleteBuilding(Long id) {
        BuildingDO building = buildingMapper.selectById(id);
        if (building == null) {
            throw new BusinessException(BaseErrorCode.BUILDING_NOT_FOUND.getCode(),
                    BaseErrorCode.BUILDING_NOT_FOUND.getMessage());
        }

        // 校验下属单元
        LambdaQueryWrapper<UnitDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UnitDO::getBuildingId, id);
        if (unitMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(BaseErrorCode.BUILDING_HAS_UNITS.getCode(),
                    BaseErrorCode.BUILDING_HAS_UNITS.getMessage());
        }

        // 逻辑删除
        buildingMapper.deleteById(id);

        baseCacheService.evictBuildingCache(id, building.getCommunityId());
        log.info("删除楼栋成功, id={}", id);
    }

    @Override
    public void changeStatus(Long id, StatusChangeDTO statusDTO) {
        BuildingDO building = buildingMapper.selectById(id);
        if (building == null) {
            throw new BusinessException(BaseErrorCode.BUILDING_NOT_FOUND.getCode(),
                    BaseErrorCode.BUILDING_NOT_FOUND.getMessage());
        }

        // 停用时校验下属启用单元
        if (BaseConstants.STATUS_DISABLED == statusDTO.getStatus()) {
            LambdaQueryWrapper<UnitDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UnitDO::getBuildingId, id)
                    .eq(UnitDO::getStatus, BaseConstants.STATUS_ENABLED);
            if (unitMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(BaseErrorCode.BUILDING_HAS_ACTIVE_UNITS.getCode(),
                        BaseErrorCode.BUILDING_HAS_ACTIVE_UNITS.getMessage());
            }
        }

        building.setStatus(statusDTO.getStatus());
        buildingMapper.updateById(building);

        baseCacheService.evictBuildingCache(id, building.getCommunityId());
        log.info("楼栋状态变更成功, id={}, status={}", id, statusDTO.getStatus());
    }

    @Override
    public ImportResultDTO importBuildings(MultipartFile file) {
        ExcelImportHelper.validateFile(file);
        try {
            BuildingExcelListener listener = new BuildingExcelListener(buildingMapper, communityMapper);
            EasyExcel.read(file.getInputStream(), BuildingExcelModel.class, listener)
                    .sheet().headRowNumber(1).doRead();

            if (listener.getSuccessCount() == 0 && listener.getErrorList().isEmpty()) {
                throw new BusinessException(BaseErrorCode.IMPORT_DATA_EMPTY.getCode(),
                        BaseErrorCode.IMPORT_DATA_EMPTY.getMessage());
            }
            return ExcelImportHelper.buildResult(listener.getSuccessCount(), listener.getErrorList(), "楼栋");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("楼栋导入失败: {}", e.getMessage(), e);
            throw new BusinessException(BaseErrorCode.IMPORT_FILE_FORMAT_ERROR.getCode(), "导入失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) {
        ExcelImportHelper.downloadTemplate(response, BuildingExcelModel.class, "楼栋导入模板", "楼栋导入模板.xlsx");
    }
}
