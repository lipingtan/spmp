package com.spmp.base.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.base.constant.BaseConstants;
import com.spmp.base.constant.BaseErrorCode;
import com.spmp.base.domain.dto.CommunityCreateDTO;
import com.spmp.base.domain.dto.CommunityPageDTO;
import com.spmp.base.domain.dto.CommunityQueryDTO;
import com.spmp.base.domain.dto.CommunityUpdateDTO;
import com.spmp.base.domain.dto.ImportResultDTO;
import com.spmp.base.domain.dto.StatusChangeDTO;
import com.spmp.base.domain.entity.BuildingDO;
import com.spmp.base.domain.entity.CommunityDO;
import com.spmp.base.domain.entity.DistrictDO;
import com.spmp.base.excel.CommunityExcelListener;
import com.spmp.base.excel.CommunityExcelModel;
import com.spmp.base.excel.ExcelImportHelper;
import com.spmp.base.repository.BuildingMapper;
import com.spmp.base.repository.CommunityMapper;
import com.spmp.base.repository.DistrictMapper;
import com.spmp.base.service.BaseCacheService;
import com.spmp.base.service.CommunityService;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 小区管理服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class CommunityServiceImpl implements CommunityService {

    private final CommunityMapper communityMapper;
    private final DistrictMapper districtMapper;
    private final BuildingMapper buildingMapper;
    private final BaseCacheService baseCacheService;

    public CommunityServiceImpl(CommunityMapper communityMapper,
                                DistrictMapper districtMapper,
                                BuildingMapper buildingMapper,
                                @Lazy BaseCacheService baseCacheService) {
        this.communityMapper = communityMapper;
        this.districtMapper = districtMapper;
        this.buildingMapper = buildingMapper;
        this.baseCacheService = baseCacheService;
    }

    @Override
    public PageResult<CommunityPageDTO> listCommunities(CommunityQueryDTO queryDTO) {
        IPage<CommunityPageDTO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<CommunityPageDTO> result = communityMapper.selectCommunityPage(page, queryDTO);
        return PageResult.of(result);
    }

    @Override
    public void createCommunity(CommunityCreateDTO createDTO) {
        // 校验所属片区启用状态
        DistrictDO district = districtMapper.selectById(createDTO.getDistrictId());
        if (district == null) {
            throw new BusinessException(BaseErrorCode.DISTRICT_NOT_FOUND.getCode(),
                    BaseErrorCode.DISTRICT_NOT_FOUND.getMessage());
        }
        if (BaseConstants.STATUS_DISABLED == district.getStatus()) {
            throw new BusinessException(BaseErrorCode.DISTRICT_DISABLED.getCode(),
                    BaseErrorCode.DISTRICT_DISABLED.getMessage());
        }

        // 名称在同一片区内唯一校验
        LambdaQueryWrapper<CommunityDO> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(CommunityDO::getCommunityName, createDTO.getCommunityName())
                .eq(CommunityDO::getDistrictId, createDTO.getDistrictId());
        if (communityMapper.selectCount(nameWrapper) > 0) {
            throw new BusinessException(BaseErrorCode.COMMUNITY_NAME_EXISTS.getCode(),
                    BaseErrorCode.COMMUNITY_NAME_EXISTS.getMessage());
        }

        // 编码处理：自动生成或手动填写唯一校验
        String code = createDTO.getCommunityCode();
        if (!StringUtils.hasText(code)) {
            code = generateCommunityCode();
        } else {
            // 手动填写编码唯一校验
            LambdaQueryWrapper<CommunityDO> codeWrapper = new LambdaQueryWrapper<>();
            codeWrapper.eq(CommunityDO::getCommunityCode, code);
            if (communityMapper.selectCount(codeWrapper) > 0) {
                throw new BusinessException(BaseErrorCode.COMMUNITY_CODE_EXISTS.getCode(),
                        BaseErrorCode.COMMUNITY_CODE_EXISTS.getMessage());
            }
        }

        CommunityDO community = new CommunityDO();
        community.setCommunityName(createDTO.getCommunityName());
        community.setCommunityCode(code);
        community.setAddress(createDTO.getAddress());
        community.setDistrictId(createDTO.getDistrictId());
        community.setPhone(createDTO.getPhone());
        community.setPropertyCompany(createDTO.getPropertyCompany());
        community.setArea(createDTO.getArea());
        community.setLongitude(createDTO.getLongitude());
        community.setLatitude(createDTO.getLatitude());
        community.setImage(createDTO.getImage());
        community.setStatus(BaseConstants.STATUS_ENABLED);
        community.setRemark(createDTO.getRemark());
        communityMapper.insert(community);

        baseCacheService.evictCommunityCache(community.getId(), createDTO.getDistrictId());
        log.info("新增小区成功, id={}, name={}, code={}", community.getId(), community.getCommunityName(), code);
    }

    @Override
    public void updateCommunity(Long id, CommunityUpdateDTO updateDTO) {
        CommunityDO community = communityMapper.selectById(id);
        if (community == null) {
            throw new BusinessException(BaseErrorCode.COMMUNITY_NOT_FOUND.getCode(),
                    BaseErrorCode.COMMUNITY_NOT_FOUND.getMessage());
        }

        // 名称唯一校验（排除自身，同一片区内）
        LambdaQueryWrapper<CommunityDO> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(CommunityDO::getCommunityName, updateDTO.getCommunityName())
                .eq(CommunityDO::getDistrictId, community.getDistrictId())
                .ne(CommunityDO::getId, id);
        if (communityMapper.selectCount(nameWrapper) > 0) {
            throw new BusinessException(BaseErrorCode.COMMUNITY_NAME_EXISTS.getCode(),
                    BaseErrorCode.COMMUNITY_NAME_EXISTS.getMessage());
        }

        // 禁止修改所属片区 — 只更新允许的字段
        community.setCommunityName(updateDTO.getCommunityName());
        community.setAddress(updateDTO.getAddress());
        community.setPhone(updateDTO.getPhone());
        community.setPropertyCompany(updateDTO.getPropertyCompany());
        community.setArea(updateDTO.getArea());
        community.setLongitude(updateDTO.getLongitude());
        community.setLatitude(updateDTO.getLatitude());
        community.setImage(updateDTO.getImage());
        community.setRemark(updateDTO.getRemark());
        communityMapper.updateById(community);

        baseCacheService.evictCommunityCache(id, community.getDistrictId());
        log.info("编辑小区成功, id={}", id);
    }

    @Override
    public void deleteCommunity(Long id) {
        CommunityDO community = communityMapper.selectById(id);
        if (community == null) {
            throw new BusinessException(BaseErrorCode.COMMUNITY_NOT_FOUND.getCode(),
                    BaseErrorCode.COMMUNITY_NOT_FOUND.getMessage());
        }

        // 校验下属楼栋
        LambdaQueryWrapper<BuildingDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BuildingDO::getCommunityId, id);
        if (buildingMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(BaseErrorCode.COMMUNITY_HAS_BUILDINGS.getCode(),
                    BaseErrorCode.COMMUNITY_HAS_BUILDINGS.getMessage());
        }

        // 逻辑删除
        communityMapper.deleteById(id);

        baseCacheService.evictCommunityCache(id, community.getDistrictId());
        log.info("删除小区成功, id={}", id);
    }

    @Override
    public void changeStatus(Long id, StatusChangeDTO statusDTO) {
        CommunityDO community = communityMapper.selectById(id);
        if (community == null) {
            throw new BusinessException(BaseErrorCode.COMMUNITY_NOT_FOUND.getCode(),
                    BaseErrorCode.COMMUNITY_NOT_FOUND.getMessage());
        }

        // 停用时校验下属启用楼栋
        if (BaseConstants.STATUS_DISABLED == statusDTO.getStatus()) {
            LambdaQueryWrapper<BuildingDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BuildingDO::getCommunityId, id)
                    .eq(BuildingDO::getStatus, BaseConstants.STATUS_ENABLED);
            if (buildingMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(BaseErrorCode.COMMUNITY_HAS_ACTIVE_BUILDINGS.getCode(),
                        BaseErrorCode.COMMUNITY_HAS_ACTIVE_BUILDINGS.getMessage());
            }
        }

        community.setStatus(statusDTO.getStatus());
        communityMapper.updateById(community);

        baseCacheService.evictCommunityCache(id, community.getDistrictId());
        log.info("小区状态变更成功, id={}, status={}", id, statusDTO.getStatus());
    }

    @Override
    public ImportResultDTO importCommunities(MultipartFile file) {
        ExcelImportHelper.validateFile(file);
        try {
            CommunityExcelListener listener = new CommunityExcelListener(communityMapper, districtMapper);
            EasyExcel.read(file.getInputStream(), CommunityExcelModel.class, listener)
                    .sheet().headRowNumber(1).doRead();

            if (listener.getSuccessCount() == 0 && listener.getErrorList().isEmpty()) {
                throw new BusinessException(BaseErrorCode.IMPORT_DATA_EMPTY.getCode(),
                        BaseErrorCode.IMPORT_DATA_EMPTY.getMessage());
            }
            return ExcelImportHelper.buildResult(listener.getSuccessCount(), listener.getErrorList(), "小区");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("小区导入失败: {}", e.getMessage(), e);
            throw new BusinessException(BaseErrorCode.IMPORT_FILE_FORMAT_ERROR.getCode(), "导入失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) {
        ExcelImportHelper.downloadTemplate(response, CommunityExcelModel.class, "小区导入模板", "小区导入模板.xlsx");
    }

    /**
     * 自动生成小区编码（COM+6位序号，MAX+1）。
     * <p>
     * 编码唯一约束重试，最多重试3次。
     *
     * @return 生成的编码
     */
    private String generateCommunityCode() {
        for (int retry = 0; retry < 3; retry++) {
            String maxCode = communityMapper.selectMaxCode();
            int nextSeq = 1;
            if (maxCode != null && maxCode.startsWith(BaseConstants.COMMUNITY_CODE_PREFIX)) {
                String seqStr = maxCode.substring(BaseConstants.COMMUNITY_CODE_PREFIX.length());
                try {
                    nextSeq = Integer.parseInt(seqStr) + 1;
                } catch (NumberFormatException e) {
                    log.warn("解析小区编码序号失败: {}", maxCode);
                }
            }
            String code = BaseConstants.COMMUNITY_CODE_PREFIX
                    + String.format("%0" + BaseConstants.CODE_SEQ_LENGTH + "d", nextSeq);

            // 校验编码唯一性
            LambdaQueryWrapper<CommunityDO> codeWrapper = new LambdaQueryWrapper<>();
            codeWrapper.eq(CommunityDO::getCommunityCode, code);
            if (communityMapper.selectCount(codeWrapper) == 0) {
                return code;
            }
            log.warn("小区编码 {} 已存在，重试生成 (retry={})", code, retry + 1);
        }
        throw new BusinessException(BaseErrorCode.CODE_GENERATE_FAILED.getCode(),
                BaseErrorCode.CODE_GENERATE_FAILED.getMessage());
    }
}
