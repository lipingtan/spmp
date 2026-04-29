package com.spmp.base.service;

import com.spmp.base.domain.dto.CommunityCreateDTO;
import com.spmp.base.domain.dto.CommunityPageDTO;
import com.spmp.base.domain.dto.CommunityQueryDTO;
import com.spmp.base.domain.dto.CommunityUpdateDTO;
import com.spmp.base.domain.dto.ImportResultDTO;
import com.spmp.base.domain.dto.StatusChangeDTO;
import com.spmp.common.result.PageResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 小区管理服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface CommunityService {

    /**
     * 分页查询小区（应用数据权限过滤，关联统计总户数）。
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<CommunityPageDTO> listCommunities(CommunityQueryDTO queryDTO);

    /**
     * 新增小区（名称片区内唯一、编码自动生成或手动填写唯一校验、校验所属片区启用状态）。
     *
     * @param createDTO 新增请求
     */
    void createCommunity(CommunityCreateDTO createDTO);

    /**
     * 编辑小区（名称唯一校验排除自身、禁止修改所属片区）。
     *
     * @param id        小区ID
     * @param updateDTO 编辑请求
     */
    void updateCommunity(Long id, CommunityUpdateDTO updateDTO);

    /**
     * 删除小区（逻辑删除，校验下属楼栋）。
     *
     * @param id 小区ID
     */
    void deleteCommunity(Long id);

    /**
     * 停用/启用小区（停用时校验下属启用楼栋）。
     *
     * @param id        小区ID
     * @param statusDTO 状态变更请求
     */
    void changeStatus(Long id, StatusChangeDTO statusDTO);

    /**
     * Excel 导入小区（Task 8 实现）。
     *
     * @param file Excel 文件
     * @return 导入结果
     */
    ImportResultDTO importCommunities(MultipartFile file);

    /**
     * 下载小区导入模板（Task 8 实现）。
     *
     * @param response HTTP 响应
     */
    void downloadImportTemplate(HttpServletResponse response);
}
