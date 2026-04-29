package com.spmp.owner.controller.h5;

import com.spmp.common.result.Result;
import com.spmp.common.util.SecurityUtils;
import com.spmp.owner.domain.dto.FamilyMemberCreateDTO;
import com.spmp.owner.domain.vo.FamilyMemberVO;
import com.spmp.owner.service.FamilyMemberService;
import com.spmp.user.annotation.OperationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * H5 业主端 — 家庭成员管理 Controller。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/owner/h5/family-members")
@RequiredArgsConstructor
public class H5FamilyMemberController {

    private final FamilyMemberService familyMemberService;

    /**
     * 家庭成员列表。
     */
    @GetMapping
    public Result<List<FamilyMemberVO>> listFamilyMembers() {
        Long ownerId = SecurityUtils.getCurrentOwnerId();
        return Result.success(familyMemberService.listByOwnerId(ownerId));
    }

    /**
     * 添加家庭成员。
     */
    @PostMapping
    @OperationLog(module = "家庭成员", type = "CREATE", description = "添加家庭成员")
    public Result<Void> addFamilyMember(@Valid @RequestBody FamilyMemberCreateDTO createDTO) {
        Long ownerId = SecurityUtils.getCurrentOwnerId();
        familyMemberService.addFamilyMember(ownerId, createDTO);
        return Result.success(null);
    }

    /**
     * 删除家庭成员。
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "家庭成员", type = "DELETE", description = "删除家庭成员")
    public Result<Void> deleteFamilyMember(@PathVariable Long id) {
        Long ownerId = SecurityUtils.getCurrentOwnerId();
        familyMemberService.deleteFamilyMember(ownerId, id);
        return Result.success(null);
    }
}
