package com.spmp.billing.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.spmp.billing.constant.BillingErrorCode;
import com.spmp.billing.constant.FeeType;
import com.spmp.billing.constant.BillingMethod;
import com.spmp.billing.domain.entity.FeeConfigDO;
import com.spmp.billing.domain.dto.FeeConfigCreateDTO;
import com.spmp.billing.domain.dto.FeeConfigUpdateDTO;
import com.spmp.billing.domain.vo.FeeConfigVO;
import com.spmp.billing.repository.FeeConfigMapper;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 费用配置 Controller（PC）。
 */
@RestController
@RequestMapping("/api/v1/billing/config")
@RequiredArgsConstructor
public class FeeConfigController {

    private final FeeConfigMapper feeConfigMapper;

    @GetMapping
    public Result<List<FeeConfigVO>> list(@RequestParam(required = false) Long communityId,
                                          @RequestParam(required = false) String feeType,
                                          @RequestParam(required = false) String status) {
        LambdaQueryWrapper<FeeConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(communityId != null, FeeConfigDO::getCommunityId, communityId)
                .eq(feeType != null && !feeType.isEmpty(), FeeConfigDO::getFeeType, feeType)
                .eq(status != null && !status.isEmpty(), FeeConfigDO::getStatus, status)
                .eq(FeeConfigDO::getDelFlag, 0)
                .orderByDesc(FeeConfigDO::getCreateTime);
        List<FeeConfigVO> data = feeConfigMapper.selectList(wrapper).stream().map(item -> {
            FeeConfigVO vo = new FeeConfigVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());
        return Result.success(data);
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody FeeConfigCreateDTO dto) {
        checkDuplicate(null, dto.getCommunityId(), dto.getBuildingId(), dto.getFeeType());
        FeeConfigDO config = new FeeConfigDO();
        BeanUtils.copyProperties(dto, config);
        config.setStatus("ENABLED");
        feeConfigMapper.insert(config);
        return Result.success(config.getId());
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody FeeConfigUpdateDTO dto) {
        FeeConfigDO old = feeConfigMapper.selectById(id);
        if (old == null) {
            throw new BusinessException(BillingErrorCode.FEE_CONFIG_NOT_FOUND.getCode(), BillingErrorCode.FEE_CONFIG_NOT_FOUND.getMessage());
        }
        checkDuplicate(id, old.getCommunityId(), old.getBuildingId(), old.getFeeType());
        BeanUtils.copyProperties(dto, old);
        feeConfigMapper.updateById(old);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        FeeConfigDO old = feeConfigMapper.selectById(id);
        if (old == null) {
            throw new BusinessException(BillingErrorCode.FEE_CONFIG_NOT_FOUND.getCode(), BillingErrorCode.FEE_CONFIG_NOT_FOUND.getMessage());
        }
        feeConfigMapper.deleteById(id);
        return Result.success(null);
    }

    private void checkDuplicate(Long id, Long communityId, Long buildingId, String feeType) {
        LambdaQueryWrapper<FeeConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FeeConfigDO::getCommunityId, communityId)
                .eq(FeeConfigDO::getFeeType, feeType)
                .eq(buildingId != null, FeeConfigDO::getBuildingId, buildingId)
                .isNull(buildingId == null, FeeConfigDO::getBuildingId)
                .eq(FeeConfigDO::getDelFlag, 0);
        if (id != null) {
            wrapper.ne(FeeConfigDO::getId, id);
        }
        long count = feeConfigMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(BillingErrorCode.FEE_CONFIG_DUPLICATE.getCode(), BillingErrorCode.FEE_CONFIG_DUPLICATE.getMessage());
        }
    }
}
