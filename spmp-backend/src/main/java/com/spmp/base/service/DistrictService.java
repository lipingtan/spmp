package com.spmp.base.service;

import com.spmp.base.domain.dto.DistrictCreateDTO;
import com.spmp.base.domain.dto.DistrictPageDTO;
import com.spmp.base.domain.dto.DistrictQueryDTO;
import com.spmp.base.domain.dto.DistrictUpdateDTO;
import com.spmp.base.domain.dto.ImportResultDTO;
import com.spmp.base.domain.dto.StatusChangeDTO;
import com.spmp.common.result.PageResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 片区管理服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface DistrictService {

    /**
     * 分页查询片区（应用数据权限过滤）。
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<DistrictPageDTO> listDistricts(DistrictQueryDTO queryDTO);

    /**
     * 新增片区（自动生成编码 DIS+6位）。
     *
     * @param createDTO 新增请求
     */
    void createDistrict(DistrictCreateDTO createDTO);

    /**
     * 编辑片区（禁止修改编码）。
     *
     * @param id        片区ID
     * @param updateDTO 编辑请求
     */
    void updateDistrict(Long id, DistrictUpdateDTO updateDTO);

    /**
     * 删除片区（逻辑删除，校验下属小区）。
     *
     * @param id 片区ID
     */
    void deleteDistrict(Long id);

    /**
     * 停用/启用片区（停用时校验下属启用小区）。
     *
     * @param id        片区ID
     * @param statusDTO 状态变更请求
     */
    void changeStatus(Long id, StatusChangeDTO statusDTO);

    /**
     * Excel 导入片区（Task 8 实现）。
     *
     * @param file Excel 文件
     * @return 导入结果
     */
    ImportResultDTO importDistricts(MultipartFile file);

    /**
     * 下载片区导入模板（Task 8 实现）。
     *
     * @param response HTTP 响应
     */
    void downloadImportTemplate(HttpServletResponse response);
}
