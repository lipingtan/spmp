package com.spmp.base.controller;

import com.spmp.base.domain.dto.BuildingCreateDTO;
import com.spmp.base.domain.dto.BuildingPageDTO;
import com.spmp.base.domain.dto.BuildingQueryDTO;
import com.spmp.base.domain.dto.BuildingUpdateDTO;
import com.spmp.base.domain.dto.ImportResultDTO;
import com.spmp.base.domain.dto.StatusChangeDTO;
import com.spmp.base.service.BuildingService;
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
 * 楼栋管理 Controller。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/base/buildings")
public class BuildingController {

    private final BuildingService buildingService;

    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @GetMapping
    @PreAuthorize("@perm.check('base:building:list')")
    public PageResult<BuildingPageDTO> listBuildings(BuildingQueryDTO queryDTO) {
        return buildingService.listBuildings(queryDTO);
    }

    @PostMapping
    @PreAuthorize("@perm.check('base:building:create')")
    @OperationLog(module = "楼栋管理", type = "CREATE", description = "新增楼栋")
    public Result<Void> createBuilding(@Valid @RequestBody BuildingCreateDTO createDTO) {
        buildingService.createBuilding(createDTO);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.check('base:building:edit')")
    @OperationLog(module = "楼栋管理", type = "UPDATE", description = "编辑楼栋")
    public Result<Void> updateBuilding(@PathVariable Long id, @Valid @RequestBody BuildingUpdateDTO updateDTO) {
        buildingService.updateBuilding(id, updateDTO);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.check('base:building:delete')")
    @OperationLog(module = "楼栋管理", type = "DELETE", description = "删除楼栋")
    public Result<Void> deleteBuilding(@PathVariable Long id) {
        buildingService.deleteBuilding(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@perm.check('base:building:edit')")
    @OperationLog(module = "楼栋管理", type = "UPDATE", description = "楼栋停用/启用")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestBody StatusChangeDTO statusDTO) {
        buildingService.changeStatus(id, statusDTO);
        return Result.success(null);
    }

    @PostMapping("/import")
    @PreAuthorize("@perm.check('base:building:import')")
    @OperationLog(module = "楼栋管理", type = "IMPORT", description = "楼栋Excel导入")
    public Result<ImportResultDTO> importBuildings(@RequestParam("file") MultipartFile file) {
        ImportResultDTO result = buildingService.importBuildings(file);
        return Result.success(result);
    }

    @GetMapping("/import-template")
    @PreAuthorize("@perm.check('base:building:import')")
    public void downloadImportTemplate(HttpServletResponse response) {
        buildingService.downloadImportTemplate(response);
    }
}
