package com.spmp.base.service;

import com.spmp.base.domain.dto.ImportResultDTO;
import com.spmp.base.domain.dto.StatusChangeDTO;
import com.spmp.base.domain.dto.UnitCreateDTO;
import com.spmp.base.domain.dto.UnitPageDTO;
import com.spmp.base.domain.dto.UnitQueryDTO;
import com.spmp.base.domain.dto.UnitUpdateDTO;
import com.spmp.common.result.PageResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 单元管理服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface UnitService {

    /**
     * 分页查询单元（应用数据权限过滤）。
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<UnitPageDTO> listUnits(UnitQueryDTO queryDTO);

    /**
     * 新增单元（编号楼栋内唯一、校验所属楼栋启用状态）。
     *
     * @param createDTO 新增请求
     */
    void createUnit(UnitCreateDTO createDTO);

    /**
     * 编辑单元（编号唯一校验排除自身、禁止修改所属楼栋）。
     *
     * @param id        单元ID
     * @param updateDTO 编辑请求
     */
    void updateUnit(Long id, UnitUpdateDTO updateDTO);

    /**
     * 删除单元（逻辑删除，校验下属房屋，存在则拒绝）。
     *
     * @param id 单元ID
     */
    void deleteUnit(Long id);

    /**
     * 停用/启用单元（停用时校验下属房屋）。
     *
     * @param id        单元ID
     * @param statusDTO 状态变更请求
     */
    void changeStatus(Long id, StatusChangeDTO statusDTO);

    /**
     * Excel 导入单元（Task 8 实现）。
     *
     * @param file Excel 文件
     * @return 导入结果
     */
    ImportResultDTO importUnits(MultipartFile file);

    /**
     * 下载单元导入模板（Task 8 实现）。
     *
     * @param response HTTP 响应
     */
    void downloadImportTemplate(HttpServletResponse response);
}
