package com.spmp.billing.controller;

import com.spmp.billing.constant.BillStatus;
import com.spmp.billing.domain.dto.BillQueryDTO;
import com.spmp.billing.domain.dto.UrgeDTO;
import com.spmp.billing.domain.entity.BillDO;
import com.spmp.billing.domain.entity.UrgeRecordDO;
import com.spmp.billing.domain.vo.BillListVO;
import com.spmp.billing.repository.BillMapper;
import com.spmp.billing.repository.BillUrgeRecordMapper;
import com.spmp.common.result.PageResult;
import com.spmp.common.result.Result;
import com.spmp.common.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 逾期管理 Controller（PC）。
 */
@RestController
@RequestMapping("/api/v1/billing/overdue")
@RequiredArgsConstructor
public class OverdueController {

    private final BillController billController;
    private final BillMapper billMapper;
    private final BillUrgeRecordMapper urgeRecordMapper;

    @GetMapping("/bills")
    public Result<PageResult<BillListVO>> listOverdue(BillQueryDTO queryDTO) {
        queryDTO.setStatus(BillStatus.OVERDUE.getCode());
        return billController.list(queryDTO);
    }

    @PostMapping("/urge")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> urge(@RequestBody UrgeDTO dto) {
        for (Long billId : dto.getBillIds()) {
            BillDO bill = billMapper.selectById(billId);
            if (bill == null) {
                continue;
            }
            UrgeRecordDO record = new UrgeRecordDO();
            record.setBillId(billId);
            record.setBillNo(bill.getBillNo());
            record.setUrgeType(dto.getUrgeType());
            record.setUrgeUserId(SecurityUtils.getCurrentUserId());
            record.setUrgeUserName("SYSTEM");
            record.setFeedback(dto.getFeedback());
            urgeRecordMapper.insert(record);
        }
        return Result.success(null);
    }
}
