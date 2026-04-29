package com.spmp.base.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.spmp.base.constant.BaseConstants;
import com.spmp.base.constant.BuildingType;
import com.spmp.base.domain.entity.BuildingDO;
import com.spmp.base.domain.entity.CommunityDO;
import com.spmp.base.repository.BuildingMapper;
import com.spmp.base.repository.CommunityMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 楼栋 Excel 导入监听器。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
public class BuildingExcelListener extends AnalysisEventListener<BuildingExcelModel> {

    private final List<BuildingDO> dataList = new ArrayList<>();
    @Getter
    private final List<ExcelErrorInfo> errorList = new ArrayList<>();
    private final BuildingMapper buildingMapper;
    private final CommunityMapper communityMapper;
    private final Map<String, CommunityDO> communityCache = new HashMap<>();
    @Getter
    private int successCount = 0;
    private int currentRow = 1;

    public BuildingExcelListener(BuildingMapper buildingMapper, CommunityMapper communityMapper) {
        this.buildingMapper = buildingMapper;
        this.communityMapper = communityMapper;
    }

    @Override
    public void invoke(BuildingExcelModel data, AnalysisContext context) {
        currentRow++;
        if (!StringUtils.hasText(data.getBuildingName())) {
            errorList.add(new ExcelErrorInfo(currentRow, "楼栋名称", "楼栋名称不能为空"));
            return;
        }
        if (!StringUtils.hasText(data.getBuildingCode())) {
            errorList.add(new ExcelErrorInfo(currentRow, "楼栋编号", "楼栋编号不能为空"));
            return;
        }
        if (!StringUtils.hasText(data.getCommunityName())) {
            errorList.add(new ExcelErrorInfo(currentRow, "所属小区名称", "所属小区名称不能为空"));
            return;
        }
        if (data.getAboveGroundFloors() == null || data.getAboveGroundFloors() < 1) {
            errorList.add(new ExcelErrorInfo(currentRow, "地上层数", "地上层数至少为1"));
            return;
        }
        if (StringUtils.hasText(data.getBuildingType()) && !BuildingType.isValid(data.getBuildingType())) {
            errorList.add(new ExcelErrorInfo(currentRow, "楼栋类型", "楼栋类型无效: " + data.getBuildingType()));
            return;
        }

        CommunityDO community = communityCache.computeIfAbsent(data.getCommunityName(), name -> {
            LambdaQueryWrapper<CommunityDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CommunityDO::getCommunityName, name)
                    .eq(CommunityDO::getStatus, BaseConstants.STATUS_ENABLED);
            return communityMapper.selectOne(wrapper);
        });
        if (community == null) {
            errorList.add(new ExcelErrorInfo(currentRow, "所属小区名称", "小区不存在或已停用: " + data.getCommunityName()));
            return;
        }

        BuildingDO building = new BuildingDO();
        building.setBuildingName(data.getBuildingName());
        building.setBuildingCode(data.getBuildingCode());
        building.setCommunityId(community.getId());
        building.setAboveGroundFloors(data.getAboveGroundFloors());
        building.setUndergroundFloors(data.getUndergroundFloors() != null ? data.getUndergroundFloors() : 0);
        building.setUnitsPerFloor(data.getUnitsPerFloor() != null ? data.getUnitsPerFloor() : 1);
        building.setBuildingType(StringUtils.hasText(data.getBuildingType()) ? data.getBuildingType() : "RESIDENTIAL");
        building.setStatus(BaseConstants.STATUS_ENABLED);
        building.setRemark(data.getRemark());

        dataList.add(building);
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
        for (BuildingDO building : dataList) {
            try {
                LambdaQueryWrapper<BuildingDO> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(BuildingDO::getBuildingCode, building.getBuildingCode())
                        .eq(BuildingDO::getCommunityId, building.getCommunityId());
                BuildingDO existing = buildingMapper.selectOne(wrapper);
                if (existing != null) {
                    building.setId(existing.getId());
                    buildingMapper.updateById(building);
                } else {
                    buildingMapper.insert(building);
                }
                successCount++;
            } catch (Exception e) {
                log.warn("导入楼栋失败, code={}: {}", building.getBuildingCode(), e.getMessage());
                errorList.add(new ExcelErrorInfo(null, "楼栋编号", "保存失败: " + e.getMessage()));
            }
        }
        dataList.clear();
    }
}
