package com.spmp.base.controller;

import com.spmp.base.domain.dto.DistrictCreateDTO;
import com.spmp.base.domain.dto.DistrictPageDTO;
import com.spmp.base.domain.dto.DistrictQueryDTO;
import com.spmp.base.domain.dto.DistrictUpdateDTO;
import com.spmp.base.domain.dto.StatusChangeDTO;
import com.spmp.base.service.DistrictService;
import com.spmp.common.result.PageResult;
import com.spmp.common.result.Result;
import com.spmp.user.annotation.OperationLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 片区管理 Controller。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/base/districts")
public class DistrictController {

    private final DistrictService districtService;

    public DistrictController(DistrictService districtService) {
        this.districtService = districtService;
    }

    /**
     * 片区分页查询。
     */
    @GetMapping
    @PreAuthorize("@perm.check('base:district:list')")
    public PageResult<DistrictPageDTO> listDistricts(DistrictQueryDTO queryDTO) {
        return districtService.listDistricts(queryDTO);
    }

    /**
     * 新增片区。
     */
    @PostMapping
    @PreAuthorize("@perm.check('base:district:create')")
    @OperationLog(module = "片区管理", type = "CREATE", description = "新增片区")
    public Result<Void> createDistrict(@Valid @RequestBody DistrictCreateDTO createDTO) {
        districtService.createDistrict(createDTO);
        return Result.success(null);
    }

    /**
     * 编辑片区。
     */
    @PutMapping("/{id}")
    @PreAuthorize("@perm.check('base:district:edit')")
    @OperationLog(module = "片区管理", type = "UPDATE", description = "编辑片区")
    public Result<Void> updateDistrict(@PathVariable Long id, @Valid @RequestBody DistrictUpdateDTO updateDTO) {
        districtService.updateDistrict(id, updateDTO);
        return Result.success(null);
    }

    /**
     * 删除片区。
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.check('base:district:delete')")
    @OperationLog(module = "片区管理", type = "DELETE", description = "删除片区")
    public Result<Void> deleteDistrict(@PathVariable Long id) {
        districtService.deleteDistrict(id);
        return Result.success(null);
    }

    /**
     * 片区停用/启用。
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("@perm.check('base:district:edit')")
    @OperationLog(module = "片区管理", type = "UPDATE", description = "片区停用/启用")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestBody StatusChangeDTO statusDTO) {
        districtService.changeStatus(id, statusDTO);
        return Result.success(null);
    }
}
