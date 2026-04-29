package com.spmp.base.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.base.constant.BaseConstants;
import com.spmp.base.constant.BaseErrorCode;
import com.spmp.base.domain.dto.ImportResultDTO;
import com.spmp.base.domain.dto.StatusChangeDTO;
import com.spmp.base.domain.dto.UnitCreateDTO;
import com.spmp.base.domain.dto.UnitPageDTO;
import com.spmp.base.domain.dto.UnitQueryDTO;
import com.spmp.base.domain.dto.UnitUpdateDTO;
import com.spmp.base.domain.entity.BuildingDO;
import com.spmp.base.domain.entity.HouseDO;
import com.spmp.base.domain.entity.UnitDO;
import com.spmp.base.excel.ExcelImportHelper;
import com.spmp.base.excel.UnitExcelListener;
import com.spmp.base.excel.UnitExcelModel;
import com.spmp.base.repository.BuildingMapper;
import com.spmp.base.repository.CommunityMapper;
import com.spmp.base.repository.HouseMapper;
import com.spmp.base.repository.UnitMapper;
import com.spmp.base.service.BaseCacheService;
import com.spmp.base.service.UnitService;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 单元管理服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class UnitServiceImpl implements UnitService {

    private final UnitMapper unitMapper;
    private final BuildingMapper buildingMapper;
    private final HouseMapper houseMapper;
    private final CommunityMapper communityMapper;
    private final BaseCacheService baseCacheService;

    public UnitServiceImpl(UnitMapper unitMapper,
                           BuildingMapper buildingMapper,
                           HouseMapper houseMapper,
                           CommunityMapper communityMapper,
                           @Lazy BaseCacheService baseCacheService) {
        this.unitMapper = unitMapper;
        this.buildingMapper = buildingMapper;
        this.houseMapper = houseMapper;
        this.communityMapper = communityMapper;
        this.baseCacheService = baseCacheService;
    }

    @Override
    public PageResult<UnitPageDTO> listUnits(UnitQueryDTO queryDTO) {
        IPage<UnitPageDTO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<UnitPageDTO> result = unitMapper.selectUnitPage(page, queryDTO);
        return PageResult.of(result);
    }

    @Override
    public void createUnit(UnitCreateDTO createDTO) {
        // 校验所属楼栋启用状态
        BuildingDO building = buildingMapper.selectById(createDTO.getBuildingId());
        if (building == null) {
            throw new BusinessException(BaseErrorCode.BUILDING_NOT_FOUND.getCode(),
                    BaseErrorCode.BUILDING_NOT_FOUND.getMessage());
        }
        if (BaseConstants.STATUS_DISABLED == building.getStatus()) {
            throw new BusinessException(BaseErrorCode.BUILDING_DISABLED.getCode(),
                    BaseErrorCode.BUILDING_DISABLED.getMessage());
        }

        // 编号在同一楼栋内唯一校验
        LambdaQueryWrapper<UnitDO> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(UnitDO::getUnitCode, createDTO.getUnitCode())
                .eq(UnitDO::getBuildingId, createDTO.getBuildingId());
        if (unitMapper.selectCount(codeWrapper) > 0) {
            throw new BusinessException(BaseErrorCode.UNIT_CODE_EXISTS.getCode(),
                    BaseErrorCode.UNIT_CODE_EXISTS.getMessage());
        }

        UnitDO unit = new UnitDO();
        unit.setUnitName(createDTO.getUnitName());
        unit.setUnitCode(createDTO.getUnitCode());
        unit.setBuildingId(createDTO.getBuildingId());
        unit.setStatus(BaseConstants.STATUS_ENABLED);
        unit.setRemark(createDTO.getRemark());
        unitMapper.insert(unit);

        // 获取楼栋的 communityId 用于缓存清除
        BuildingDO parentBuilding = buildingMapper.selectById(createDTO.getBuildingId());
        Long communityId = parentBuilding != null ? parentBuilding.getCommunityId() : null;
        baseCacheService.evictUnitCache(unit.getId(), createDTO.getBuildingId(), communityId);
        log.info("新增单元成功, id={}, name={}, code={}", unit.getId(), unit.getUnitName(), unit.getUnitCode());
    }

    @Override
    public void updateUnit(Long id, UnitUpdateDTO updateDTO) {
        UnitDO unit = unitMapper.selectById(id);
        if (unit == null) {
            throw new BusinessException(BaseErrorCode.UNIT_NOT_FOUND.getCode(),
                    BaseErrorCode.UNIT_NOT_FOUND.getMessage());
        }

        // 编号唯一校验（排除自身，同一楼栋内）
        LambdaQueryWrapper<UnitDO> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(UnitDO::getUnitCode, updateDTO.getUnitCode())
                .eq(UnitDO::getBuildingId, unit.getBuildingId())
                .ne(UnitDO::getId, id);
        if (unitMapper.selectCount(codeWrapper) > 0) {
            throw new BusinessException(BaseErrorCode.UNIT_CODE_EXISTS.getCode(),
                    BaseErrorCode.UNIT_CODE_EXISTS.getMessage());
        }

        // 禁止修改所属楼栋 — 只更新允许的字段
        unit.setUnitName(updateDTO.getUnitName());
        unit.setUnitCode(updateDTO.getUnitCode());
        unit.setRemark(updateDTO.getRemark());
        unitMapper.updateById(unit);

        BuildingDO parentBuilding = buildingMapper.selectById(unit.getBuildingId());
        Long communityId = parentBuilding != null ? parentBuilding.getCommunityId() : null;
        baseCacheService.evictUnitCache(id, unit.getBuildingId(), communityId);
        log.info("编辑单元成功, id={}", id);
    }

    @Override
    public void deleteUnit(Long id) {
        UnitDO unit = unitMapper.selectById(id);
        if (unit == null) {
            throw new BusinessException(BaseErrorCode.UNIT_NOT_FOUND.getCode(),
                    BaseErrorCode.UNIT_NOT_FOUND.getMessage());
        }

        // 校验下属房屋（存在则拒绝，提示"只能停用不能删除"）
        LambdaQueryWrapper<HouseDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseDO::getUnitId, id);
        if (houseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(BaseErrorCode.UNIT_HAS_HOUSES.getCode(),
                    BaseErrorCode.UNIT_HAS_HOUSES.getMessage());
        }

        // 逻辑删除
        unitMapper.deleteById(id);

        BuildingDO parentBuilding = buildingMapper.selectById(unit.getBuildingId());
        Long communityId = parentBuilding != null ? parentBuilding.getCommunityId() : null;
        baseCacheService.evictUnitCache(id, unit.getBuildingId(), communityId);
        log.info("删除单元成功, id={}", id);
    }

    @Override
    public void changeStatus(Long id, StatusChangeDTO statusDTO) {
        UnitDO unit = unitMapper.selectById(id);
        if (unit == null) {
            throw new BusinessException(BaseErrorCode.UNIT_NOT_FOUND.getCode(),
                    BaseErrorCode.UNIT_NOT_FOUND.getMessage());
        }

        // 停用时校验下属房屋
        if (BaseConstants.STATUS_DISABLED == statusDTO.getStatus()) {
            LambdaQueryWrapper<HouseDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(HouseDO::getUnitId, id);
            if (houseMapper.selectCount(wrapper) > 0) {
                log.info("单元 id={} 下存在房屋，执行停用操作", id);
            }
        }

        unit.setStatus(statusDTO.getStatus());
        unitMapper.updateById(unit);

        BuildingDO parentBuilding = buildingMapper.selectById(unit.getBuildingId());
        Long communityId = parentBuilding != null ? parentBuilding.getCommunityId() : null;
        baseCacheService.evictUnitCache(id, unit.getBuildingId(), communityId);
        log.info("单元状态变更成功, id={}, status={}", id, statusDTO.getStatus());
    }

    @Override
    public ImportResultDTO importUnits(MultipartFile file) {
        ExcelImportHelper.validateFile(file);
        try {
            UnitExcelListener listener = new UnitExcelListener(unitMapper, buildingMapper, communityMapper);
            EasyExcel.read(file.getInputStream(), UnitExcelModel.class, listener)
                    .sheet().headRowNumber(1).doRead();

            if (listener.getSuccessCount() == 0 && listener.getErrorList().isEmpty()) {
                throw new BusinessException(BaseErrorCode.IMPORT_DATA_EMPTY.getCode(),
                        BaseErrorCode.IMPORT_DATA_EMPTY.getMessage());
            }
            return ExcelImportHelper.buildResult(listener.getSuccessCount(), listener.getErrorList(), "单元");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("单元导入失败: {}", e.getMessage(), e);
            throw new BusinessException(BaseErrorCode.IMPORT_FILE_FORMAT_ERROR.getCode(), "导入失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) {
        ExcelImportHelper.downloadTemplate(response, UnitExcelModel.class, "单元导入模板", "单元导入模板.xlsx");
    }
}
