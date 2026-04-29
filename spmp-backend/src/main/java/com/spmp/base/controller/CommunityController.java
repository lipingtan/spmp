package com.spmp.base.controller;

import com.spmp.base.domain.dto.CommunityCreateDTO;
import com.spmp.base.domain.dto.CommunityPageDTO;
import com.spmp.base.domain.dto.CommunityQueryDTO;
import com.spmp.base.domain.dto.CommunityUpdateDTO;
import com.spmp.base.domain.dto.ImportResultDTO;
import com.spmp.base.domain.dto.StatusChangeDTO;
import com.spmp.base.service.CommunityService;
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
 * 小区管理 Controller。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/base/communities")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    /**
     * 小区分页查询。
     */
    @GetMapping
    @PreAuthorize("@perm.check('base:community:list')")
    public PageResult<CommunityPageDTO> listCommunities(CommunityQueryDTO queryDTO) {
        return communityService.listCommunities(queryDTO);
    }

    /**
     * 新增小区。
     */
    @PostMapping
    @PreAuthorize("@perm.check('base:community:create')")
    @OperationLog(module = "小区管理", type = "CREATE", description = "新增小区")
    public Result<Void> createCommunity(@Valid @RequestBody CommunityCreateDTO createDTO) {
        communityService.createCommunity(createDTO);
        return Result.success(null);
    }

    /**
     * 编辑小区。
     */
    @PutMapping("/{id}")
    @PreAuthorize("@perm.check('base:community:edit')")
    @OperationLog(module = "小区管理", type = "UPDATE", description = "编辑小区")
    public Result<Void> updateCommunity(@PathVariable Long id, @Valid @RequestBody CommunityUpdateDTO updateDTO) {
        communityService.updateCommunity(id, updateDTO);
        return Result.success(null);
    }

    /**
     * 删除小区。
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.check('base:community:delete')")
    @OperationLog(module = "小区管理", type = "DELETE", description = "删除小区")
    public Result<Void> deleteCommunity(@PathVariable Long id) {
        communityService.deleteCommunity(id);
        return Result.success(null);
    }

    /**
     * 小区停用/启用。
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("@perm.check('base:community:edit')")
    @OperationLog(module = "小区管理", type = "UPDATE", description = "小区停用/启用")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestBody StatusChangeDTO statusDTO) {
        communityService.changeStatus(id, statusDTO);
        return Result.success(null);
    }

    /**
     * 小区 Excel 导入。
     */
    @PostMapping("/import")
    @PreAuthorize("@perm.check('base:community:import')")
    @OperationLog(module = "小区管理", type = "IMPORT", description = "小区Excel导入")
    public Result<ImportResultDTO> importCommunities(@RequestParam("file") MultipartFile file) {
        ImportResultDTO result = communityService.importCommunities(file);
        return Result.success(result);
    }

    /**
     * 下载小区导入模板。
     */
    @GetMapping("/import-template")
    @PreAuthorize("@perm.check('base:community:import')")
    public void downloadImportTemplate(HttpServletResponse response) {
        communityService.downloadImportTemplate(response);
    }
}
