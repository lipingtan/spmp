package com.spmp.base.service;

import com.spmp.base.domain.dto.HouseCreateDTO;
import com.spmp.base.domain.dto.HousePageDTO;
import com.spmp.base.domain.dto.HouseQueryDTO;
import com.spmp.base.domain.dto.HouseStatusChangeDTO;
import com.spmp.base.domain.dto.HouseUpdateDTO;
import com.spmp.base.domain.dto.ImportResultDTO;
import com.spmp.base.domain.entity.HouseStatusLogDO;
import com.spmp.common.result.PageResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 房屋管理服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface HouseService {

    /**
     * 分页查询房屋（多级 JOIN，应用数据权限过滤）。
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<HousePageDTO> listHouses(HouseQueryDTO queryDTO);

    /**
     * 新增房屋（编号单元内唯一、校验所属单元启用状态、校验房屋状态/类型枚举值、记录初始状态到状态变更历史）。
     *
     * @param createDTO 新增请求
     */
    void createHouse(HouseCreateDTO createDTO);

    /**
     * 编辑房屋（编号唯一校验排除自身、禁止修改所属单元）。
     *
     * @param id        房屋ID
     * @param updateDTO 编辑请求
     */
    void updateHouse(Long id, HouseUpdateDTO updateDTO);

    /**
     * 删除房屋（逻辑删除）。
     *
     * @param id 房屋ID
     */
    void deleteHouse(Long id);

    /**
     * 房屋状态变更（更新房屋状态、记录变更历史到 bs_house_status_log）。
     *
     * @param id        房屋ID
     * @param statusDTO 状态变更请求
     */
    void changeHouseStatus(Long id, HouseStatusChangeDTO statusDTO);

    /**
     * 查询房屋状态变更历史。
     *
     * @param id 房屋ID
     * @return 状态变更历史列表
     */
    List<HouseStatusLogDO> getStatusLogs(Long id);

    /**
     * Excel 导入房屋（Task 8 实现）。
     *
     * @param file Excel 文件
     * @return 导入结果
     */
    ImportResultDTO importHouses(MultipartFile file);

    /**
     * 下载房屋导入模板（Task 8 实现）。
     *
     * @param response HTTP 响应
     */
    void downloadImportTemplate(HttpServletResponse response);
}
