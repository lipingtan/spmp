package com.spmp.workorder.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.spmp.common.exception.BusinessException;
import com.spmp.workorder.config.WorkOrderEventPublisher;
import com.spmp.workorder.config.WorkOrderStateMachine;
import com.spmp.workorder.constant.*;
import com.spmp.workorder.domain.dto.RepairMaterialDTO;
import com.spmp.workorder.domain.dto.WorkOrderCancelDTO;
import com.spmp.workorder.domain.dto.WorkOrderCompleteDTO;
import com.spmp.workorder.domain.dto.WorkOrderDispatchDTO;
import com.spmp.workorder.domain.dto.WorkOrderVerifyDTO;
import com.spmp.workorder.domain.entity.*;
import com.spmp.workorder.repository.*;
import com.spmp.workorder.service.impl.WorkOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("工单Service测试")
class WorkOrderServiceTest {

    @Mock
    private WorkOrderMapper workOrderMapper;

    @Mock
    private WorkOrderImageMapper workOrderImageMapper;

    @Mock
    private DispatchRecordMapper dispatchRecordMapper;

    @Mock
    private RepairMaterialMapper repairMaterialMapper;

    @Mock
    private EvaluationMapper evaluationMapper;

    @Mock
    private WorkOrderLogMapper workOrderLogMapper;

    @Mock
    private WorkOrderStateMachine stateMachine;

    @Mock
    private WorkOrderEventPublisher eventPublisher;

    @InjectMocks
    private WorkOrderServiceImpl workOrderService;

    private WorkOrderDO sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = new WorkOrderDO();
        sampleOrder.setId(1L);
        sampleOrder.setOrderNo("WO202604190001");
        sampleOrder.setStatus(WorkOrderStatus.PENDING_DISPATCH.getCode());
        sampleOrder.setReporterId(100L);
        sampleOrder.setCommunityId(10L);
    }

    @Nested
    @DisplayName("派发工单测试")
    class DispatchTests {

        @Test
        @DisplayName("正常派发工单")
        void testDispatchSuccess() {
            WorkOrderDispatchDTO dispatchDTO = new WorkOrderDispatchDTO();
            dispatchDTO.setRepairUserId(200L);
            dispatchDTO.setRemark("请尽快处理");

            when(workOrderMapper.selectById(1L)).thenReturn(sampleOrder);
            doNothing().when(stateMachine).checkTransition(any(), any());
            when(workOrderMapper.updateById(any())).thenReturn(1);
            when(dispatchRecordMapper.insert(any())).thenReturn(1);
            when(workOrderLogMapper.insert(any())).thenReturn(1);

            assertDoesNotThrow(() -> workOrderService.dispatchWorkOrder(1L, dispatchDTO));

            verify(workOrderMapper).updateById(any(WorkOrderDO.class));
            verify(dispatchRecordMapper).insert(any(DispatchRecordDO.class));
            verify(workOrderLogMapper).insert(any(WorkOrderLogDO.class));
            verify(eventPublisher).publishDispatch(eq(1L), eq(200L), anyString());
        }

        @Test
        @DisplayName("派发不存在的工单")
        void testDispatchNotFound() {
            WorkOrderDispatchDTO dispatchDTO = new WorkOrderDispatchDTO();
            dispatchDTO.setRepairUserId(200L);

            when(workOrderMapper.selectById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                () -> workOrderService.dispatchWorkOrder(999L, dispatchDTO));
            assertEquals(WorkOrderErrorCode.WORK_ORDER_NOT_FOUND.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("派发非待派发状态的工单")
        void testDispatchInvalidStatus() {
            sampleOrder.setStatus(WorkOrderStatus.IN_PROGRESS.getCode());
            WorkOrderDispatchDTO dispatchDTO = new WorkOrderDispatchDTO();
            dispatchDTO.setRepairUserId(200L);

            when(workOrderMapper.selectById(1L)).thenReturn(sampleOrder);
            doThrow(new BusinessException(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), "状态不允许"))
                .when(stateMachine).checkTransition(any(), any());

            BusinessException ex = assertThrows(BusinessException.class,
                () -> workOrderService.dispatchWorkOrder(1L, dispatchDTO));
            assertEquals(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), ex.getCode());
        }
    }

    @Nested
    @DisplayName("接单测试")
    class AcceptTests {

        @Test
        @DisplayName("正常接单")
        void testAcceptSuccess() {
            sampleOrder.setStatus(WorkOrderStatus.PENDING_ACCEPT.getCode());
            sampleOrder.setRepairUserId(200L);

            when(workOrderMapper.selectById(1L)).thenReturn(sampleOrder);
            when(workOrderMapper.updateById(any())).thenReturn(1);
            when(workOrderLogMapper.insert(any())).thenReturn(1);

            assertDoesNotThrow(() -> workOrderService.acceptWorkOrder(1L, 200L));

            verify(workOrderMapper).updateById(argThat(order ->
                WorkOrderStatus.IN_PROGRESS.getCode().equals(order.getStatus()) &&
                order.getActualStartTime() != null
            ));
            verify(eventPublisher).publishAccept(eq(1L), eq(100L), anyString());
        }

        @Test
        @DisplayName("接单时工单状态不是待接单")
        void testAcceptInvalidStatus() {
            sampleOrder.setStatus(WorkOrderStatus.PENDING_DISPATCH.getCode());

            when(workOrderMapper.selectById(1L)).thenReturn(sampleOrder);

            BusinessException ex = assertThrows(BusinessException.class,
                () -> workOrderService.acceptWorkOrder(1L, 200L));
            assertEquals(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("接单时非派发给该维修人员")
        void testAcceptNotYours() {
            sampleOrder.setStatus(WorkOrderStatus.PENDING_ACCEPT.getCode());
            sampleOrder.setRepairUserId(300L);

            when(workOrderMapper.selectById(1L)).thenReturn(sampleOrder);

            BusinessException ex = assertThrows(BusinessException.class,
                () -> workOrderService.acceptWorkOrder(1L, 200L));
            assertEquals(WorkOrderErrorCode.WORK_ORDER_NOT_YOURS.getCode(), ex.getCode());
        }
    }

    @Nested
    @DisplayName("完成维修测试")
    class CompleteTests {

        @Test
        @DisplayName("正常完成维修")
        void testCompleteSuccess() {
            sampleOrder.setStatus(WorkOrderStatus.IN_PROGRESS.getCode());
            sampleOrder.setRepairUserId(200L);
            sampleOrder.setReporterId(100L);

            WorkOrderCompleteDTO completeDTO = new WorkOrderCompleteDTO();
            completeDTO.setRepairDescription("已更换水龙头");
            completeDTO.setRepairDuration(60);
            completeDTO.setImageUrls(Arrays.asList("http://example.com/img1.jpg"));
            RepairMaterialDTO material = new RepairMaterialDTO();
            material.setMaterialName("水龙头");
            material.setQuantity(new BigDecimal("1"));
            material.setUnit("个");
            material.setUnitPrice(new BigDecimal("50"));
            completeDTO.setMaterials(Collections.singletonList(material));

            when(workOrderMapper.selectById(1L)).thenReturn(sampleOrder);
            when(workOrderMapper.updateById(any())).thenReturn(1);
            when(workOrderImageMapper.insert(any())).thenReturn(1);
            when(repairMaterialMapper.insert(any())).thenReturn(1);
            when(workOrderLogMapper.insert(any())).thenReturn(1);

            assertDoesNotThrow(() -> workOrderService.completeWorkOrder(1L, completeDTO, 200L));

            verify(workOrderMapper).updateById(argThat(order ->
                WorkOrderStatus.PENDING_VERIFY.getCode().equals(order.getStatus())
            ));
            verify(workOrderImageMapper).insert(any(WorkOrderImageDO.class));
            verify(repairMaterialMapper).insert(any(RepairMaterialDO.class));
            verify(eventPublisher).publishComplete(eq(1L), eq(100L), anyString());
        }

        @Test
        @DisplayName("完成时非处理中状态")
        void testCompleteInvalidStatus() {
            sampleOrder.setStatus(WorkOrderStatus.PENDING_ACCEPT.getCode());

            WorkOrderCompleteDTO completeDTO = new WorkOrderCompleteDTO();

            when(workOrderMapper.selectById(1L)).thenReturn(sampleOrder);

            BusinessException ex = assertThrows(BusinessException.class,
                () -> workOrderService.completeWorkOrder(1L, completeDTO, 200L));
            assertEquals(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), ex.getCode());
        }
    }

    @Nested
    @DisplayName("转派测试")
    class TransferTests {

        @Test
        @DisplayName("正常转派")
        void testTransferSuccess() {
            sampleOrder.setStatus(WorkOrderStatus.IN_PROGRESS.getCode());
            sampleOrder.setRepairUserId(200L);

            when(workOrderMapper.selectById(1L)).thenReturn(sampleOrder);
            when(workOrderMapper.updateById(any())).thenReturn(1);
            when(workOrderLogMapper.insert(any())).thenReturn(1);

            assertDoesNotThrow(() -> workOrderService.transferWorkOrder(1L, 200L, "需要更专业的维修人员"));

            verify(workOrderMapper).updateById(argThat(order ->
                WorkOrderStatus.PENDING_DISPATCH.getCode().equals(order.getStatus()) &&
                order.getRepairUserId() == null
            ));
        }

        @Test
        @DisplayName("转派时非处理中状态")
        void testTransferInvalidStatus() {
            sampleOrder.setStatus(WorkOrderStatus.PENDING_ACCEPT.getCode());

            when(workOrderMapper.selectById(1L)).thenReturn(sampleOrder);

            BusinessException ex = assertThrows(BusinessException.class,
                () -> workOrderService.transferWorkOrder(1L, 200L, "原因"));
            assertEquals(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), ex.getCode());
        }
    }

    @Nested
    @DisplayName("验收测试")
    class VerifyTests {

        @Test
        @DisplayName("验收通过")
        void testVerifyPass() {
            sampleOrder.setStatus(WorkOrderStatus.PENDING_VERIFY.getCode());
            sampleOrder.setReporterId(100L);

            WorkOrderVerifyDTO verifyDTO = new WorkOrderVerifyDTO();
            verifyDTO.setPassed(true);
            verifyDTO.setScore(5);
            verifyDTO.setEvaluateContent("服务很好");

            when(workOrderMapper.selectById(1L)).thenReturn(sampleOrder);
            when(workOrderMapper.updateById(any())).thenReturn(1);
            when(evaluationMapper.insert(any())).thenReturn(1);
            when(workOrderLogMapper.insert(any())).thenReturn(1);

            assertDoesNotThrow(() -> workOrderService.verifyWorkOrder(1L, verifyDTO, 100L));

            verify(workOrderMapper).updateById(argThat(order ->
                WorkOrderStatus.COMPLETED.getCode().equals(order.getStatus())
            ));
            verify(evaluationMapper).insert(any(EvaluationDO.class));
        }

        @Test
        @DisplayName("验收不通过")
        void testVerifyReject() {
            sampleOrder.setStatus(WorkOrderStatus.PENDING_VERIFY.getCode());
            sampleOrder.setReporterId(100L);
            sampleOrder.setRepairUserId(200L);

            WorkOrderVerifyDTO verifyDTO = new WorkOrderVerifyDTO();
            verifyDTO.setPassed(false);
            verifyDTO.setRejectReason("未修复完成");
            verifyDTO.setRejectImageUrls(Arrays.asList("http://example.com/reject.jpg"));

            when(workOrderMapper.selectById(1L)).thenReturn(sampleOrder);
            when(workOrderMapper.updateById(any())).thenReturn(1);
            when(workOrderImageMapper.insert(any())).thenReturn(1);
            when(workOrderLogMapper.insert(any())).thenReturn(1);

            assertDoesNotThrow(() -> workOrderService.verifyWorkOrder(1L, verifyDTO, 100L));

            verify(workOrderMapper).updateById(argThat(order ->
                WorkOrderStatus.IN_PROGRESS.getCode().equals(order.getStatus()) &&
                order.getRejectCount() == 1
            ));
            verify(eventPublisher).publishVerifyReject(eq(1L), eq(200L), anyString(), eq(1));
        }

        @Test
        @DisplayName("验收不通过时缺少原因")
        void testVerifyRejectMissingReason() {
            sampleOrder.setStatus(WorkOrderStatus.PENDING_VERIFY.getCode());
            sampleOrder.setReporterId(100L);

            WorkOrderVerifyDTO verifyDTO = new WorkOrderVerifyDTO();
            verifyDTO.setPassed(false);
            verifyDTO.setRejectReason(null);

            when(workOrderMapper.selectById(1L)).thenReturn(sampleOrder);

            BusinessException ex = assertThrows(BusinessException.class,
                () -> workOrderService.verifyWorkOrder(1L, verifyDTO, 100L));
            assertEquals(WorkOrderErrorCode.VERIFY_REJECT_REASON_REQUIRED.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("验收不通过时缺少图片")
        void testVerifyRejectMissingImage() {
            sampleOrder.setStatus(WorkOrderStatus.PENDING_VERIFY.getCode());
            sampleOrder.setReporterId(100L);

            WorkOrderVerifyDTO verifyDTO = new WorkOrderVerifyDTO();
            verifyDTO.setPassed(false);
            verifyDTO.setRejectReason("未修复");
            verifyDTO.setRejectImageUrls(Collections.emptyList());

            when(workOrderMapper.selectById(1L)).thenReturn(sampleOrder);

            BusinessException ex = assertThrows(BusinessException.class,
                () -> workOrderService.verifyWorkOrder(1L, verifyDTO, 100L));
            assertEquals(WorkOrderErrorCode.VERIFY_REJECT_IMAGE_REQUIRED.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("验收不通过次数超限")
        void testVerifyRejectLimitExceeded() {
            sampleOrder.setStatus(WorkOrderStatus.PENDING_VERIFY.getCode());
            sampleOrder.setReporterId(100L);
            sampleOrder.setRejectCount(3);

            WorkOrderVerifyDTO verifyDTO = new WorkOrderVerifyDTO();
            verifyDTO.setPassed(false);
            verifyDTO.setRejectReason("未修复");
            verifyDTO.setRejectImageUrls(Arrays.asList("http://example.com/reject.jpg"));

            when(workOrderMapper.selectById(1L)).thenReturn(sampleOrder);

            BusinessException ex = assertThrows(BusinessException.class,
                () -> workOrderService.verifyWorkOrder(1L, verifyDTO, 100L));
            assertEquals(WorkOrderErrorCode.REJECT_LIMIT_EXCEEDED.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("非本人工单")
        void testVerifyNotYours() {
            sampleOrder.setStatus(WorkOrderStatus.PENDING_VERIFY.getCode());
            sampleOrder.setReporterId(100L);

            WorkOrderVerifyDTO verifyDTO = new WorkOrderVerifyDTO();
            verifyDTO.setPassed(true);

            when(workOrderMapper.selectById(1L)).thenReturn(sampleOrder);

            BusinessException ex = assertThrows(BusinessException.class,
                () -> workOrderService.verifyWorkOrder(1L, verifyDTO, 999L));
            assertEquals(WorkOrderErrorCode.WORK_ORDER_NOT_YOURS.getCode(), ex.getCode());
        }
    }

    @Nested
    @DisplayName("取消工单测试")
    class CancelTests {

        @Test
        @DisplayName("正常取消待派发工单")
        void testCancelPendingDispatch() {
            WorkOrderCancelDTO cancelDTO = new WorkOrderCancelDTO();
            cancelDTO.setCancelReason("不需要维修了");
            cancelDTO.setCancelType("CANCEL");

            when(workOrderMapper.selectById(1L)).thenReturn(sampleOrder);
            when(workOrderMapper.updateById(any())).thenReturn(1);
            when(workOrderLogMapper.insert(any())).thenReturn(1);

            assertDoesNotThrow(() -> workOrderService.cancelWorkOrder(1L, cancelDTO, 100L, "OWNER"));

            verify(workOrderMapper).updateById(argThat(order ->
                WorkOrderStatus.CANCELLED.getCode().equals(order.getStatus()) &&
                "不需要维修了".equals(order.getCancelReason())
            ));
        }

        @Test
        @DisplayName("强制关闭处理中工单")
        void testForceClose() {
            sampleOrder.setStatus(WorkOrderStatus.IN_PROGRESS.getCode());
            sampleOrder.setRepairUserId(200L);

            WorkOrderCancelDTO cancelDTO = new WorkOrderCancelDTO();
            cancelDTO.setCancelReason("工单异常需关闭");
            cancelDTO.setCancelType("FORCE_CLOSE");

            when(workOrderMapper.selectById(1L)).thenReturn(sampleOrder);
            when(workOrderMapper.updateById(any())).thenReturn(1);
            when(workOrderLogMapper.insert(any())).thenReturn(1);

            assertDoesNotThrow(() -> workOrderService.cancelWorkOrder(1L, cancelDTO, 300L, "ADMIN"));

            verify(workOrderMapper).updateById(argThat(order ->
                WorkOrderStatus.FORCE_CLOSED.getCode().equals(order.getStatus())
            ));
            verify(eventPublisher).publishCancel(eq(1L), eq(200L), anyString());
        }

        @Test
        @DisplayName("取消已派发的工单时通知维修人员")
        void testCancelAfterDispatchNotifiesRepairStaff() {
            sampleOrder.setStatus(WorkOrderStatus.PENDING_ACCEPT.getCode());
            sampleOrder.setRepairUserId(200L);

            WorkOrderCancelDTO cancelDTO = new WorkOrderCancelDTO();
            cancelDTO.setCancelReason("不需要了");
            cancelDTO.setCancelType("CANCEL");

            when(workOrderMapper.selectById(1L)).thenReturn(sampleOrder);
            when(workOrderMapper.updateById(any())).thenReturn(1);
            when(workOrderLogMapper.insert(any())).thenReturn(1);

            assertDoesNotThrow(() -> workOrderService.cancelWorkOrder(1L, cancelDTO, 100L, "OWNER"));

            verify(eventPublisher).publishCancel(eq(1L), eq(200L), anyString());
        }
    }
}