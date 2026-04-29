package com.spmp.base.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.spmp.base.constant.BaseConstants;
import com.spmp.base.domain.entity.BuildingDO;
import com.spmp.base.domain.entity.CommunityDO;
import com.spmp.base.domain.entity.UnitDO;
import com.spmp.base.repository.BuildingMapper;
import com.spmp.base.repository.CommunityMapper;
import com.spmp.base.repository.UnitMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单元 Excel 导入监听器。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
public class UnitExcelListener extends AnalysisEventListener<UnitExcelModel> {

    private final List<UnitDO> dataList = new ArrayList<>();
    @Getter
    private final List<ExcelErrorInfo> errorList = new ArrayList<>();
    private final UnitMapper unitMapper;
    private final BuildingMapper buildingMapper;
    private final CommunityMapper communityMapper;
    /** key: communityName + "|" + buildingCode → BuildingDO */
    private final Map<String, BuildingDO> buildingCache = new HashMap<>();
    @Getter
    private int successCount = 0;
    private int currentRow = 1;

    public UnitExcelListener(UnitMapper unitMapper, BuildingMapper buildingMapper, CommunityMapper communityMapper) {
        this.unitMapper = unitMapper;
        this.buildingMapper = buildingMapper;
        this.communityMapper = communityMapper;
    }

    @Override
    public void invoke(UnitExcelModel data, AnalysisContext context) {
        currentRow++;
        if (!StringUtils.hasText(data.getUnitName())) {
            errorList.add(new ExcelErrorInfo(currentRow, "单元名称", "单元名称不能为空"));
            return;
        }
        if (!StringUtils.hasText(data.getUnitCode())) {
            errorList.add(new ExcelErrorInfo(currentRow, "单元编号", "单元编号不能为空"));
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

        String cacheKey = data.getCommunityName() + "|" + data.getBuildingCode();
        BuildingDO building = buildingCache.computeIfAbsent(cacheKey, k -> {
            // 先查小区
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
            return buildingMapper.selectOne(bw);
        });
        if (building == null) {
            errorList.add(new ExcelErrorInfo(currentRow, "所属楼栋编号", "楼栋不存在或已停用"));
            return;
        }

        UnitDO unit = new UnitDO();
        unit.setUnitName(data.getUnitName());
        unit.setUnitCode(data.getUnitCode());
        unit.setBuildingId(building.getId());
        unit.setStatus(BaseConstants.STATUS_ENABLED);
        unit.setRemark(data.getRemark());

        dataList.add(unit);
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
        for (UnitDO unit : dataList) {
            try {
                LambdaQueryWrapper<UnitDO> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(UnitDO::getUnitCode, unit.getUnitCode())
                        .eq(UnitDO::getBuildingId, unit.getBuildingId());
                UnitDO existing = unitMapper.selectOne(wrapper);
                if (existing != null) {
                    unit.setId(existing.getId());
                    unitMapper.updateById(unit);
                } else {
                    unitMapper.insert(unit);
                }
                successCount++;
            } catch (Exception e) {
                log.warn("导入单元失败, code={}: {}", unit.getUnitCode(), e.getMessage());
                errorList.add(new ExcelErrorInfo(null, "单元编号", "保存失败: " + e.getMessage()));
            }
        }
        dataList.clear();
    }
}
