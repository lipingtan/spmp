package com.spmp.base.service;

import com.spmp.base.domain.dto.BuildingCreateDTO;
import com.spmp.base.domain.dto.BuildingPageDTO;
import com.spmp.base.domain.dto.BuildingQueryDTO;
import com.spmp.base.domain.dto.BuildingUpdateDTO;
import com.spmp.base.domain.dto.ImportResultDTO;
import com.spmp.base.domain.dto.StatusChangeDTO;
import com.spmp.common.result.PageResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 楼栋管理服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface BuildingService {

    /**
     * 分页查询楼栋（应用数据权限过滤）。
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<BuildingPageDTO> listBuildings(BuildingQueryDTO queryDTO);

    /**
     * 新增楼栋（编号小区内唯一、校验所属小区启用状态、校验楼栋类型枚举值）。
     *
     * @param createDTO 新增请求
     */
    void createBuilding(BuildingCreateDTO createDTO);

    /**
     * 编辑楼栋（编号唯一校验排除自身、禁止修改所属小区）。
     *
     * @param id        楼栋ID
     * @param updateDTO 编辑请求
     */
    void updateBuilding(Long id, BuildingUpdateDTO updateDTO);

    /**
     * 删除楼栋（逻辑删除，校验下属单元）。
     *
     * @param id 楼栋ID
     */
    void deleteBuilding(Long id);

    /**
     * 停用/启用楼栋（停用时校验下属启用单元）。
     *
     * @param id        楼栋ID
     * @param statusDTO 状态变更请求
     */
    void changeStatus(Long id, StatusChangeDTO statusDTO);

    /**
     * Excel 导入楼栋（Task 8 实现）。
     *
     * @param file Excel 文件
     * @return 导入结果
     */
    ImportResultDTO importBuildings(MultipartFile file);

    /**
     * 下载楼栋导入模板（Task 8 实现）。
     *
     * @param response HTTP 响应
     */
    void downloadImportTemplate(HttpServletResponse response);
}
