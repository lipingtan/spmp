package com.spmp.base.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.spmp.base.constant.BaseConstants;
import com.spmp.base.constant.HouseStatus;
import com.spmp.base.constant.HouseType;
import com.spmp.base.domain.entity.BuildingDO;
import com.spmp.base.domain.entity.CommunityDO;
import com.spmp.base.domain.entity.HouseDO;
import com.spmp.base.domain.entity.UnitDO;
import com.spmp.base.repository.BuildingMapper;
import com.spmp.base.repository.CommunityMapper;
import com.spmp.base.repository.HouseMapper;
import com.spmp.base.repository.UnitMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 房屋 Excel 导入监听器。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
public class HouseExcelListener extends AnalysisEventListener<HouseExcelModel> {

    private final List<HouseDO> dataList = new ArrayList<>();
    @Getter
    private final List<ExcelErrorInfo> errorList = new ArrayList<>();
    private final HouseMapper houseMapper;
    private final UnitMapper unitMapper;
    private final BuildingMapper buildingMapper;
    private final CommunityMapper communityMapper;
    /** key: communityName + "|" + buildingCode + "|" + unitCode → UnitDO */
    private final Map<String, UnitDO> unitCache = new HashMap<>();
    @Getter
    private int successCount = 0;
    private int currentRow = 1;

    public HouseExcelListener(HouseMapper houseMapper, UnitMapper unitMapper,
                              BuildingMapper buildingMapper, CommunityMapper communityMapper) {
        this.houseMapper = houseMapper;
        this.unitMapper = unitMapper;
        this.buildingMapper = buildingMapper;
        this.communityMapper = communityMapper;
    }

    @Override
    public void invoke(HouseExcelModel data, AnalysisContext context) {
        currentRow++;
        if (!StringUtils.hasText(data.getHouseCode())) {
            errorList.add(new ExcelErrorInfo(currentRow, "房屋编号", "房屋编号不能为空"));
            return;
        }
        if (!StringUtils.hasText(data.getCommunityName())) {
            errorList.add(new ExcelErrorInfo(currentRow, "所属小区名称", "所属小区名称不能为空"));
            return;
        }
        if (!StringUtils.hasText(data.getBuildingCode())) {
            errorList.add(new ExcelErrorInfo(currentRow, "所属楼栋编号", "所属楼栋编号不能为空"));
            return;
        }
        if (!StringUtils.hasText(data.getUnitCode())) {
            errorList.add(new ExcelErrorInfo(currentRow, "所属单元编号", "所属单元编号不能为空"));
            return;
        }
        if (data.getFloor() == null) {
            errorList.add(new ExcelErrorInfo(currentRow, "楼层", "楼层不能为空"));
            return;
        }
        if (data.getBuildingArea() == null) {
            errorList.add(new ExcelErrorInfo(currentRow, "建筑面积", "建筑面积不能为空"));
            return;
        }
        if (StringUtils.hasText(data.getHouseStatus()) && !HouseStatus.isValid(data.getHouseStatus())) {
            errorList.add(new ExcelErrorInfo(currentRow, "房屋状态", "房屋状态无效: " + data.getHouseStatus()));
            return;
        }
        if (StringUtils.hasText(data.getHouseType()) && !HouseType.isValid(data.getHouseType())) {
            errorList.add(new ExcelErrorInfo(currentRow, "房屋类型", "房屋类型无效: " + data.getHouseType()));
            return;
        }

        String cacheKey = data.getCommunityName() + "|" + data.getBuildingCode() + "|" + data.getUnitCode();
        UnitDO unit = unitCache.computeIfAbsent(cacheKey, k -> {
            LambdaQueryWrapper<CommunityDO> cw = new LambdaQueryWrapper<>();
            cw.eq(CommunityDO::getCommunityName, data.getCommunityName())
                    .eq(CommunityDO::getStatus, BaseConstants.STATUS_ENABLED);
            CommunityDO community = communityMapper.selectOne(cw);
            if (community == null) {
                return null;
            }
            LambdaQueryWrapper<BuildingDO> bw = new LambdaQueryWrapper<>();
            bw.eq(BuildingDO::getBuildingCode, data.getBuildingCode())
                    .eq(BuildingDO::getCommunityId, community.getId())
                    .eq(BuildingDO::getStatus, BaseConstants.STATUS_ENABLED);
            BuildingDO building = buildingMapper.selectOne(bw);
            if (building == null) {
                return null;
            }
            LambdaQueryWrapper<UnitDO> uw = new LambdaQueryWrapper<>();
            uw.eq(UnitDO::getUnitCode, data.getUnitCode())
                    .eq(UnitDO::getBuildingId, building.getId())
                    .eq(UnitDO::getStatus, BaseConstants.STATUS_ENABLED);
            return unitMapper.selectOne(uw);
        });
        if (unit == null) {
            errorList.add(new ExcelErrorInfo(currentRow, "所属单元编号", "单元不存在或已停用"));
            return;
        }

        HouseDO house = new HouseDO();
        house.setHouseCode(data.getHouseCode());
        house.setUnitId(unit.getId());
        house.setFloor(data.getFloor());
        house.setBuildingArea(data.getBuildingArea());
        house.setUsableArea(data.getUsableArea());
        house.setHouseStatus(StringUtils.hasText(data.getHouseStatus()) ? data.getHouseStatus() : "VACANT");
        house.setHouseType(StringUtils.hasText(data.getHouseType()) ? data.getHouseType() : "RESIDENCE");
        house.setRemark(data.getRemark());

        dataList.add(house);
        if (dataList.size() >= BaseConstants.EXCEL_BATCH_SIZE) {
            saveBatch();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!dataList.isEmpty()) {
            saveBatch();
        }
    }

    private void saveBatch() {
        for (HouseDO house : dataList) {
            try {
                LambdaQueryWrapper<HouseDO> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(HouseDO::getHouseCode, house.getHouseCode())
                        .eq(HouseDO::getUnitId, house.getUnitId());
                HouseDO existing = houseMapper.selectOne(wrapper);
                if (existing != null) {
                    house.setId(existing.getId());
                    houseMapper.updateById(house);
                } else {
                    houseMapper.insert(house);
                }
                successCount++;
            } catch (Exception e) {
                log.warn("导入房屋失败, code={}: {}", house.getHouseCode(), e.getMessage());
                errorList.add(new ExcelErrorInfo(null, "房屋编号", "保存失败: " + e.getMessage()));
            }
        }
        dataList.clear();
    }
}
