package com.spmp.base.controller;

import com.spmp.base.domain.dto.ImportResultDTO;
import com.spmp.base.domain.dto.StatusChangeDTO;
import com.spmp.base.domain.dto.UnitCreateDTO;
import com.spmp.base.domain.dto.UnitPageDTO;
import com.spmp.base.domain.dto.UnitQueryDTO;
import com.spmp.base.domain.dto.UnitUpdateDTO;
import com.spmp.base.service.UnitService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 单元管理 Controller。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/base/units")
public class UnitController {

    private final UnitService unitService;

    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    @GetMapping
    @PreAuthorize("@perm.check('base:unit:list')")
    public PageResult<UnitPageDTO> listUnits(UnitQueryDTO queryDTO) {
        return unitService.listUnits(queryDTO);
    }

    @PostMapping
    @PreAuthorize("@perm.check('base:unit:create')")
    @OperationLog(module = "单元管理", type = "CREATE", description = "新增单元")
    public Result<Void> createUnit(@Valid @RequestBody UnitCreateDTO createDTO) {
        unitService.createUnit(createDTO);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.check('base:unit:edit')")
    @OperationLog(module = "单元管理", type = "UPDATE", description = "编辑单元")
    public Result<Void> updateUnit(@PathVariable Long id, @Valid @RequestBody UnitUpdateDTO updateDTO) {
        unitService.updateUnit(id, updateDTO);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.check('base:unit:delete')")
    @OperationLog(module = "单元管理", type = "DELETE", description = "删除单元")
    public Result<Void> deleteUnit(@PathVariable Long id) {
        unitService.deleteUnit(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@perm.check('base:unit:edit')")
    @OperationLog(module = "单元管理", type = "UPDATE", description = "单元停用/启用")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestBody StatusChangeDTO statusDTO) {
        unitService.changeStatus(id, statusDTO);
        return Result.success(null);
    }

    @PostMapping("/import")
    @PreAuthorize("@perm.check('base:unit:import')")
    @OperationLog(module = "单元管理", type = "IMPORT", description = "单元Excel导入")
    public Result<ImportResultDTO> importUnits(@RequestParam("file") MultipartFile file) {
        ImportResultDTO result = unitService.importUnits(file);
        return Result.success(result);
    }

    @GetMapping("/import-template")
    @PreAuthorize("@perm.check('base:unit:import')")
    public void downloadImportTemplate(HttpServletResponse response) {
        unitService.downloadImportTemplate(response);
    }
}
