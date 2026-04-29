package com.spmp.owner.controller;

import com.spmp.common.result.PageResult;
import com.spmp.common.result.Result;
import com.spmp.owner.domain.dto.OwnerCreateDTO;
import com.spmp.owner.domain.dto.OwnerQueryDTO;
import com.spmp.owner.domain.dto.OwnerStatusDTO;
import com.spmp.owner.domain.dto.OwnerUpdateDTO;
import com.spmp.owner.domain.vo.OwnerListVO;
import com.spmp.owner.domain.vo.OwnerVO;
import com.spmp.owner.service.OwnerService;
import com.spmp.user.annotation.OperationLog;
import lombok.RequiredArgsConstructor;
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
 * 业主信息管理 Controller（PC 管理端）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/owner/owners")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    /**
     * 业主列表（分页查询，数据权限过滤）。
     */
    @GetMapping
    @PreAuthorize("@perm.check('owner:owner:list')")
    public PageResult<OwnerListVO> listOwners(OwnerQueryDTO queryDTO) {
        return ownerService.listOwners(queryDTO);
    }

    /**
     * 新增业主。
     */
    @PostMapping
    @PreAuthorize("@perm.check('owner:owner:create')")
    @OperationLog(module = "业主管理", type = "CREATE", description = "新增业主")
    public Result<Void> createOwner(@Valid @RequestBody OwnerCreateDTO createDTO) {
        ownerService.createOwner(createDTO);
        return Result.success(null);
    }

    /**
     * 业主详情。
     */
    @GetMapping("/{id}")
    @PreAuthorize("@perm.check('owner:owner:list')")
    public Result<OwnerVO> getOwnerDetail(@PathVariable Long id) {
        return Result.success(ownerService.getOwnerDetail(id));
    }

    /**
     * 编辑业主。
     */
    @PutMapping("/{id}")
    @PreAuthorize("@perm.check('owner:owner:edit')")
    @OperationLog(module = "业主管理", type = "UPDATE", description = "编辑业主")
    public Result<Void> updateOwner(@PathVariable Long id, @Valid @RequestBody OwnerUpdateDTO updateDTO) {
        ownerService.updateOwner(id, updateDTO);
        return Result.success(null);
    }

    /**
     * 删除业主。
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.check('owner:owner:delete')")
    @OperationLog(module = "业主管理", type = "DELETE", description = "删除业主")
    public Result<Void> deleteOwner(@PathVariable Long id) {
        ownerService.deleteOwner(id);
        return Result.success(null);
    }

    /**
     * 停用/启用业主。
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("@perm.check('owner:owner:edit')")
    @OperationLog(module = "业主管理", type = "UPDATE", description = "停用/启用业主")
    public Result<Void> changeOwnerStatus(@PathVariable Long id, @Valid @RequestBody OwnerStatusDTO statusDTO) {
        ownerService.changeOwnerStatus(id, statusDTO);
        return Result.success(null);
    }
}
