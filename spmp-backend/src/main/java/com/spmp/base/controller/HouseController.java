package com.spmp.base.controller;

import com.spmp.base.domain.dto.HouseCreateDTO;
import com.spmp.base.domain.dto.HousePageDTO;
import com.spmp.base.domain.dto.HouseQueryDTO;
import com.spmp.base.domain.dto.HouseStatusChangeDTO;
import com.spmp.base.domain.dto.HouseUpdateDTO;
import com.spmp.base.domain.dto.ImportResultDTO;
import com.spmp.base.domain.entity.HouseStatusLogDO;
import com.spmp.base.service.HouseService;
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
import java.util.List;

/**
 * 房屋管理 Controller。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/base/houses")
public class HouseController {

    private final HouseService houseService;

    public HouseController(HouseService houseService) {
        this.houseService = houseService;
    }

    @GetMapping
    @PreAuthorize("@perm.check('base:house:list')")
    public PageResult<HousePageDTO> listHouses(HouseQueryDTO queryDTO) {
        return houseService.listHouses(queryDTO);
    }

    @PostMapping
    @PreAuthorize("@perm.check('base:house:create')")
    @OperationLog(module = "房屋管理", type = "CREATE", description = "新增房屋")
    public Result<Void> createHouse(@Valid @RequestBody HouseCreateDTO createDTO) {
        houseService.createHouse(createDTO);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.check('base:house:edit')")
    @OperationLog(module = "房屋管理", type = "UPDATE", description = "编辑房屋")
    public Result<Void> updateHouse(@PathVariable Long id, @Valid @RequestBody HouseUpdateDTO updateDTO) {
        houseService.updateHouse(id, updateDTO);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.check('base:house:delete')")
    @OperationLog(module = "房屋管理", type = "DELETE", description = "删除房屋")
    public Result<Void> deleteHouse(@PathVariable Long id) {
        houseService.deleteHouse(id);
        return Result.success(null);
    }

    /**
     * 房屋状态变更。
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("@perm.check('base:house:edit')")
    @OperationLog(module = "房屋管理", type = "UPDATE", description = "房屋状态变更")
    public Result<Void> changeHouseStatus(@PathVariable Long id, @RequestBody HouseStatusChangeDTO statusDTO) {
        houseService.changeHouseStatus(id, statusDTO);
        return Result.success(null);
    }

    /**
     * 查询房屋状态变更历史。
     */
    @GetMapping("/{id}/status-logs")
    @PreAuthorize("@perm.check('base:house:list')")
    public Result<List<HouseStatusLogDO>> getStatusLogs(@PathVariable Long id) {
        List<HouseStatusLogDO> logs = houseService.getStatusLogs(id);
        return Result.success(logs);
    }

    @PostMapping("/import")
    @PreAuthorize("@perm.check('base:house:import')")
    @OperationLog(module = "房屋管理", type = "IMPORT", description = "房屋Excel导入")
    public Result<ImportResultDTO> importHouses(@RequestParam("file") MultipartFile file) {
        ImportResultDTO result = houseService.importHouses(file);
        return Result.success(result);
    }

    @GetMapping("/import-template")
    @PreAuthorize("@perm.check('base:house:import')")
    public void downloadImportTemplate(HttpServletResponse response) {
        houseService.downloadImportTemplate(response);
    }
}
