package com.spmp.billing.controller.h5;

import com.spmp.billing.domain.dto.H5BillQueryDTO;
import com.spmp.billing.domain.dto.H5PaymentCreateDTO;
import com.spmp.billing.domain.vo.H5BillDetailVO;
import com.spmp.billing.domain.vo.H5BillListVO;
import com.spmp.billing.domain.vo.PaymentVO;
import com.spmp.billing.service.H5BillService;
import com.spmp.common.result.PageResult;
import com.spmp.common.result.Result;
import com.spmp.user.annotation.OperationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * H5 端缴费 Controller。
 */
@RestController
@RequestMapping("/api/v1/billing/h5")
@RequiredArgsConstructor
public class H5BillController {

    private final H5BillService h5BillService;

    @GetMapping("/bills/mine")
    public PageResult<H5BillListVO> myBills(H5BillQueryDTO queryDTO) {
        Long ownerId = h5BillService.getCurrentOwnerId();
        return h5BillService.listMyBills(ownerId, queryDTO);
    }

    @GetMapping("/bills/{id}")
    public Result<H5BillDetailVO> billDetail(@PathVariable Long id) {
        Long ownerId = h5BillService.getCurrentOwnerId();
        return Result.success(h5BillService.getBillDetail(id, ownerId));
    }

    @PostMapping("/payments")
    @OperationLog(module = "缴费管理", type = "CREATE_PAYMENT", description = "H5创建支付")
    public Result<Long> createPayment(@Valid @RequestBody H5PaymentCreateDTO dto) {
        Long ownerId = h5BillService.getCurrentOwnerId();
        return Result.success(h5BillService.createPayment(dto, ownerId));
    }

    @PostMapping("/payments/callback")
    @OperationLog(module = "缴费管理", type = "PAYMENT_CALLBACK", description = "H5模拟支付回调")
    public Result<Void> callback(@RequestParam Long paymentId, @RequestParam(required = false) String callbackSign) {
        h5BillService.paymentCallback(paymentId, callbackSign);
        return Result.success(null);
    }

    @GetMapping("/payments/{id}")
    public Result<PaymentVO> paymentDetail(@PathVariable Long id) {
        Long ownerId = h5BillService.getCurrentOwnerId();
        return Result.success(h5BillService.getPaymentDetail(id, ownerId));
    }

    @GetMapping("/payments/history")
    public PageResult<PaymentVO> paymentHistory(@RequestParam(required = false) Integer pageNum,
                                                @RequestParam(required = false) Integer pageSize) {
        Long ownerId = h5BillService.getCurrentOwnerId();
        return h5BillService.listPaymentHistory(ownerId, pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
    }
}
