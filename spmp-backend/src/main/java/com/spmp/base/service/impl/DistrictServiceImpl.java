package com.spmp.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.base.constant.BaseConstants;
import com.spmp.base.constant.BaseErrorCode;
import com.spmp.base.domain.dto.DistrictCreateDTO;
import com.spmp.base.domain.dto.DistrictPageDTO;
import com.spmp.base.domain.dto.DistrictQueryDTO;
import com.spmp.base.domain.dto.DistrictUpdateDTO;
import com.spmp.base.domain.dto.ImportResultDTO;
import com.spmp.base.domain.dto.StatusChangeDTO;
import com.spmp.base.domain.entity.CommunityDO;
import com.spmp.base.domain.entity.DistrictDO;
import com.spmp.base.repository.CommunityMapper;
import com.spmp.base.repository.DistrictMapper;
import com.spmp.base.service.BaseCacheService;
import com.spmp.base.service.DistrictService;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 片区管理服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class DistrictServiceImpl implements DistrictService {

    private final DistrictMapper districtMapper;
    private final CommunityMapper communityMapper;
    private final BaseCacheService baseCacheService;

    public DistrictServiceImpl(DistrictMapper districtMapper,
                               CommunityMapper communityMapper,
                               @Lazy BaseCacheService baseCacheService) {
        this.districtMapper = districtMapper;
        this.communityMapper = communityMapper;
        this.baseCacheService = baseCacheService;
    }

    @Override
    public PageResult<DistrictPageDTO> listDistricts(DistrictQueryDTO queryDTO) {
        IPage<DistrictPageDTO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<DistrictPageDTO> result = districtMapper.selectDistrictPage(page, queryDTO);
        return PageResult.of(result);
    }

    @Override
    public void createDistrict(DistrictCreateDTO createDTO) {
        // 名称全局唯一校验
        LambdaQueryWrapper<DistrictDO> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(DistrictDO::getDistrictName, createDTO.getDistrictName());
        if (districtMapper.selectCount(nameWrapper) > 0) {
            throw new BusinessException(BaseErrorCode.DISTRICT_NAME_EXISTS.getCode(),
                    BaseErrorCode.DISTRICT_NAME_EXISTS.getMessage());
        }

        // 自动生成编码（DIS+6位，MAX+1），编码唯一约束重试
        String code = generateDistrictCode();

        DistrictDO district = new DistrictDO();
        district.setDistrictName(createDTO.getDistrictName());
        district.setDistrictCode(code);
        district.setStatus(BaseConstants.STATUS_ENABLED);
        district.setRemark(createDTO.getRemark());
        districtMapper.insert(district);

        baseCacheService.evictDistrictCache(district.getId());
        log.info("新增片区成功, id={}, name={}, code={}", district.getId(), district.getDistrictName(), code);
    }

    @Override
    public void updateDistrict(Long id, DistrictUpdateDTO updateDTO) {
        DistrictDO district = districtMapper.selectById(id);
        if (district == null) {
            throw new BusinessException(BaseErrorCode.DISTRICT_NOT_FOUND.getCode(),
                    BaseErrorCode.DISTRICT_NOT_FOUND.getMessage());
        }

        // 名称唯一校验（排除自身）
        LambdaQueryWrapper<DistrictDO> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(DistrictDO::getDistrictName, updateDTO.getDistrictName())
                .ne(DistrictDO::getId, id);
        if (districtMapper.selectCount(nameWrapper) > 0) {
            throw new BusinessException(BaseErrorCode.DISTRICT_NAME_EXISTS.getCode(),
                    BaseErrorCode.DISTRICT_NAME_EXISTS.getMessage());
        }

        // 禁止修改编码 — 只更新允许的字段
        district.setDistrictName(updateDTO.getDistrictName());
        district.setRemark(updateDTO.getRemark());
        districtMapper.updateById(district);

        baseCacheService.evictDistrictCache(id);
        log.info("编辑片区成功, id={}", id);
    }

    @Override
    public void deleteDistrict(Long id) {
        DistrictDO district = districtMapper.selectById(id);
        if (district == null) {
            throw new BusinessException(BaseErrorCode.DISTRICT_NOT_FOUND.getCode(),
                    BaseErrorCode.DISTRICT_NOT_FOUND.getMessage());
        }

        // 校验下属小区（存在则拒绝）
        int communityCount = communityMapper.countByDistrictId(id);
        if (communityCount > 0) {
            throw new BusinessException(BaseErrorCode.DISTRICT_HAS_COMMUNITIES.getCode(),
                    BaseErrorCode.DISTRICT_HAS_COMMUNITIES.getMessage());
        }

        // 逻辑删除
        districtMapper.deleteById(id);

        baseCacheService.evictDistrictCache(id);
        log.info("删除片区成功, id={}", id);
    }

    @Override
    public void changeStatus(Long id, StatusChangeDTO statusDTO) {
        DistrictDO district = districtMapper.selectById(id);
        if (district == null) {
            throw new BusinessException(BaseErrorCode.DISTRICT_NOT_FOUND.getCode(),
                    BaseErrorCode.DISTRICT_NOT_FOUND.getMessage());
        }

        // 停用时校验下属启用小区
        if (BaseConstants.STATUS_DISABLED == statusDTO.getStatus()) {
            LambdaQueryWrapper<CommunityDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CommunityDO::getDistrictId, id)
                    .eq(CommunityDO::getStatus, BaseConstants.STATUS_ENABLED);
            if (communityMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(BaseErrorCode.DISTRICT_HAS_ACTIVE_COMMUNITIES.getCode(),
                        BaseErrorCode.DISTRICT_HAS_ACTIVE_COMMUNITIES.getMessage());
            }
        }

        district.setStatus(statusDTO.getStatus());
        districtMapper.updateById(district);

        baseCacheService.evictDistrictCache(id);
        log.info("片区状态变更成功, id={}, status={}", id, statusDTO.getStatus());
    }

    @Override
    public ImportResultDTO importDistricts(MultipartFile file) {
        // Task 8 实现
        throw new UnsupportedOperationException("片区导入功能将在 Task 8 中实现");
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) {
        // Task 8 实现
        throw new UnsupportedOperationException("片区导入模板下载将在 Task 8 中实现");
    }

    /**
     * 自动生成片区编码（DIS+6位序号，MAX+1）。
     * <p>
     * 编码唯一约束重试，最多重试3次。
     *
     * @return 生成的编码
     */
    private String generateDistrictCode() {
        for (int retry = 0; retry < 3; retry++) {
            String maxCode = districtMapper.selectMaxCode();
            int nextSeq = 1;
            if (maxCode != null && maxCode.startsWith(BaseConstants.DISTRICT_CODE_PREFIX)) {
                String seqStr = maxCode.substring(BaseConstants.DISTRICT_CODE_PREFIX.length());
                try {
                    nextSeq = Integer.parseInt(seqStr) + 1;
                } catch (NumberFormatException e) {
                    log.warn("解析片区编码序号失败: {}", maxCode);
                }
            }
            String code = BaseConstants.DISTRICT_CODE_PREFIX
                    + String.format("%0" + BaseConstants.CODE_SEQ_LENGTH + "d", nextSeq);

            // 校验编码唯一性
            LambdaQueryWrapper<DistrictDO> codeWrapper = new LambdaQueryWrapper<>();
            codeWrapper.eq(DistrictDO::getDistrictCode, code);
            if (districtMapper.selectCount(codeWrapper) == 0) {
                return code;
            }
            log.warn("片区编码 {} 已存在，重试生成 (retry={})", code, retry + 1);
        }
        throw new BusinessException(BaseErrorCode.CODE_GENERATE_FAILED.getCode(),
                BaseErrorCode.CODE_GENERATE_FAILED.getMessage());
    }
}
