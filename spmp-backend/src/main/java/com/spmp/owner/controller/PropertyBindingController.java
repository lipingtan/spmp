package com.spmp.owner.controller;

import com.spmp.common.result.Result;
import com.spmp.owner.domain.dto.PropertyBindingCreateDTO;
import com.spmp.owner.service.PropertyBindingService;
import com.spmp.user.annotation.OperationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 房产绑定管理 Controller（PC 管理端）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/owner/property-bindings")
@RequiredArgsConstructor
public class PropertyBindingController {

    private final PropertyBindingService propertyBindingService;

    /**
     * 房产绑定。
     */
    @PostMapping
    @PreAuthorize("@perm.check('owner:property:binding')")
    @OperationLog(module = "房产绑定", type = "CREATE", description = "房产绑定")
    public Result<Void> bindProperty(@Valid @RequestBody PropertyBindingCreateDTO createDTO) {
        propertyBindingService.bindProperty(createDTO);
        return Result.success(null);
    }

    /**
     * 解除绑定。
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.check('owner:property:unbind')")
    @OperationLog(module = "房产绑定", type = "DELETE", description = "解除绑定")
    public Result<Void> unbindProperty(@PathVariable Long id) {
        propertyBindingService.unbindProperty(id);
        return Result.success(null);
    }
}
