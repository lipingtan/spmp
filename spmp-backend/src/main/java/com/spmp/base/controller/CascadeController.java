package com.spmp.base.controller;

import com.spmp.base.domain.vo.CascadeTreeVO;
import com.spmp.base.domain.vo.CascadeVO;
import com.spmp.base.service.CascadeService;
import com.spmp.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 级联查询 Controller。
 * <p>
 * 级联查询接口需认证但不需要特定权限，已在 SecurityConfig 白名单中配置。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/base/cascade")
public class CascadeController {

    private final CascadeService cascadeService;

    public CascadeController(CascadeService cascadeService) {
        this.cascadeService = cascadeService;
    }

    /**
     * 级联数据查询。
     *
     * @param level    级别（DISTRICT/COMMUNITY/BUILDING/UNIT/HOUSE）
     * @param parentId 父级ID（查询片区列表时不传）
     * @return 直接子级列表
     */
    @GetMapping
    public Result<List<CascadeVO>> getCascadeData(
            @RequestParam String level,
            @RequestParam(required = false) Long parentId) {
        List<CascadeVO> data = cascadeService.getCascadeData(level, parentId);
        return Result.success(data);
    }

    /**
     * 完整树查询（小区下楼栋→单元→房屋）。
     *
     * @param communityId 小区ID
     * @return 完整树形结构
     */
    @GetMapping("/tree")
    public Result<List<CascadeTreeVO>> getCascadeTree(@RequestParam Long communityId) {
        List<CascadeTreeVO> tree = cascadeService.getCascadeTree(communityId);
        return Result.success(tree);
    }
}
