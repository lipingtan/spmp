package com.spmp.base.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.spmp.base.constant.BaseConstants;
import com.spmp.base.domain.entity.CommunityDO;
import com.spmp.base.domain.entity.DistrictDO;
import com.spmp.base.repository.CommunityMapper;
import com.spmp.base.repository.DistrictMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小区 Excel 导入监听器。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
public class CommunityExcelListener extends AnalysisEventListener<CommunityExcelModel> {

    private final List<CommunityDO> dataList = new ArrayList<>();
    @Getter
    private final List<ExcelErrorInfo> errorList = new ArrayList<>();
    private final CommunityMapper communityMapper;
    private final DistrictMapper districtMapper;
    /** 片区名称 → 片区DO 缓存 */
    private final Map<String, DistrictDO> districtCache = new HashMap<>();
    @Getter
    private int successCount = 0;
    private int currentRow = 1;

    public CommunityExcelListener(CommunityMapper communityMapper, DistrictMapper districtMapper) {
        this.communityMapper = communityMapper;
        this.districtMapper = districtMapper;
    }

    @Override
    public void invoke(CommunityExcelModel data, AnalysisContext context) {
        currentRow++;
        // 校验必填字段
        if (!StringUtils.hasText(data.getCommunityName())) {
            errorList.add(new ExcelErrorInfo(currentRow, "小区名称", "小区名称不能为空"));
            return;
        }
        if (!StringUtils.hasText(data.getAddress())) {
            errorList.add(new ExcelErrorInfo(currentRow, "小区地址", "小区地址不能为空"));
            return;
        }
        if (!StringUtils.hasText(data.getDistrictName())) {
            errorList.add(new ExcelErrorInfo(currentRow, "所属片区名称", "所属片区名称不能为空"));
            return;
        }

        // 校验片区名称是否存在
        DistrictDO district = districtCache.computeIfAbsent(data.getDistrictName(), name -> {
            LambdaQueryWrapper<DistrictDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DistrictDO::getDistrictName, name)
                    .eq(DistrictDO::getStatus, BaseConstants.STATUS_ENABLED);
            return districtMapper.selectOne(wrapper);
        });
        if (district == null) {
            errorList.add(new ExcelErrorInfo(currentRow, "所属片区名称", "片区不存在或已停用: " + data.getDistrictName()));
            return;
        }

        CommunityDO community = new CommunityDO();
        community.setCommunityName(data.getCommunityName());
        community.setAddress(data.getAddress());
        community.setDistrictId(district.getId());
        community.setPhone(data.getPhone());
        community.setPropertyCompany(data.getPropertyCompany());
        community.setArea(data.getArea());
        community.setStatus(BaseConstants.STATUS_ENABLED);

        // 编码处理
        if (StringUtils.hasText(data.getCommunityCode())) {
            community.setCommunityCode(data.getCommunityCode());
        }

        dataList.add(community);
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
        for (CommunityDO community : dataList) {
            try {
                if (StringUtils.hasText(community.getCommunityCode())) {
                    // 编码已存在则覆盖更新
                    LambdaQueryWrapper<CommunityDO> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(CommunityDO::getCommunityCode, community.getCommunityCode());
                    CommunityDO existing = communityMapper.selectOne(wrapper);
                    if (existing != null) {
                        community.setId(existing.getId());
                        communityMapper.updateById(community);
                    } else {
                        communityMapper.insert(community);
                    }
                } else {
                    // 自动生成编码
                    String maxCode = communityMapper.selectMaxCode();
                    int nextSeq = 1;
                    if (maxCode != null && maxCode.startsWith(BaseConstants.COMMUNITY_CODE_PREFIX)) {
                        try {
                            nextSeq = Integer.parseInt(maxCode.substring(BaseConstants.COMMUNITY_CODE_PREFIX.length())) + 1;
                        } catch (NumberFormatException e) {
                            log.warn("解析小区编码序号失败: {}", maxCode);
                        }
                    }
                    community.setCommunityCode(BaseConstants.COMMUNITY_CODE_PREFIX
                            + String.format("%0" + BaseConstants.CODE_SEQ_LENGTH + "d", nextSeq));
                    communityMapper.insert(community);
                }
                successCount++;
            } catch (Exception e) {
                log.warn("导入小区失败, name={}: {}", community.getCommunityName(), e.getMessage());
                errorList.add(new ExcelErrorInfo(null, "小区名称", "保存失败: " + e.getMessage()));
            }
        }
        dataList.clear();
    }
}
