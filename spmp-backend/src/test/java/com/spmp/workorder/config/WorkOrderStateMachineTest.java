package com.spmp.workorder.config;

import com.spmp.common.exception.BusinessException;
import com.spmp.workorder.constant.WorkOrderErrorCode;
import com.spmp.workorder.constant.WorkOrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("工单状态机测试")
class WorkOrderStateMachineTest {

    private WorkOrderStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new WorkOrderStateMachine();
    }

    @Nested
    @DisplayName("合法状态流转测试")
    class ValidTransitions {

        @Test
        @DisplayName("待派发 -> 待接单（派发）")
        void testPendingDispatchToPendingAccept() {
            assertDoesNotThrow(() ->
                stateMachine.checkTransition(WorkOrderStatus.PENDING_DISPATCH, WorkOrderStatus.PENDING_ACCEPT)
            );
        }

        @Test
        @DisplayName("待派发 -> 已取消（取消）")
        void testPendingDispatchToCancelled() {
            assertDoesNotThrow(() ->
                stateMachine.checkTransition(WorkOrderStatus.PENDING_DISPATCH, WorkOrderStatus.CANCELLED)
            );
        }

        @Test
        @DisplayName("待接单 -> 处理中（接单）")
        void testPendingAcceptToInProgress() {
            assertDoesNotThrow(() ->
                stateMachine.checkTransition(WorkOrderStatus.PENDING_ACCEPT, WorkOrderStatus.IN_PROGRESS)
            );
        }

        @Test
        @DisplayName("待接单 -> 待派发（超时退回）")
        void testPendingAcceptToPendingDispatch() {
            assertDoesNotThrow(() ->
                stateMachine.checkTransition(WorkOrderStatus.PENDING_ACCEPT, WorkOrderStatus.PENDING_DISPATCH)
            );
        }

        @Test
        @DisplayName("待接单 -> 已取消（取消）")
        void testPendingAcceptToCancelled() {
            assertDoesNotThrow(() ->
                stateMachine.checkTransition(WorkOrderStatus.PENDING_ACCEPT, WorkOrderStatus.CANCELLED)
            );
        }

        @Test
        @DisplayName("处理中 -> 待验收（完成维修）")
        void testInProgressToPendingVerify() {
            assertDoesNotThrow(() ->
                stateMachine.checkTransition(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.PENDING_VERIFY)
            );
        }

        @Test
        @DisplayName("处理中 -> 待派发（转派）")
        void testInProgressToPendingDispatch() {
            assertDoesNotThrow(() ->
                stateMachine.checkTransition(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.PENDING_DISPATCH)
            );
        }

        @Test
        @DisplayName("处理中 -> 已关闭（强制关闭）")
        void testInProgressToForceClosed() {
            assertDoesNotThrow(() ->
                stateMachine.checkTransition(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.FORCE_CLOSED)
            );
        }

        @Test
        @DisplayName("待验收 -> 已完成（验收通过）")
        void testPendingVerifyToCompleted() {
            assertDoesNotThrow(() ->
                stateMachine.checkTransition(WorkOrderStatus.PENDING_VERIFY, WorkOrderStatus.COMPLETED)
            );
        }

        @Test
        @DisplayName("待验收 -> 处理中（验收不通过）")
        void testPendingVerifyToInProgress() {
            assertDoesNotThrow(() ->
                stateMachine.checkTransition(WorkOrderStatus.PENDING_VERIFY, WorkOrderStatus.IN_PROGRESS)
            );
        }

        @Test
        @DisplayName("待验收 -> 已关闭（强制关闭）")
        void testPendingVerifyToForceClosed() {
            assertDoesNotThrow(() ->
                stateMachine.checkTransition(WorkOrderStatus.PENDING_VERIFY, WorkOrderStatus.FORCE_CLOSED)
            );
        }
    }

    @Nested
    @DisplayName("非法状态流转测试")
    class InvalidTransitions {

        @Test
        @DisplayName("已完成 -> 处理中（非法）")
        void testCompletedToInProgress() {
            BusinessException ex = assertThrows(BusinessException.class, () ->
                stateMachine.checkTransition(WorkOrderStatus.COMPLETED, WorkOrderStatus.IN_PROGRESS)
            );
            assertEquals(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("已取消 -> 待派发（非法）")
        void testCancelledToPendingDispatch() {
            BusinessException ex = assertThrows(BusinessException.class, () ->
                stateMachine.checkTransition(WorkOrderStatus.CANCELLED, WorkOrderStatus.PENDING_DISPATCH)
            );
            assertEquals(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("已关闭 -> 待验收（非法）")
        void testForceClosedToPendingVerify() {
            BusinessException ex = assertThrows(BusinessException.class, () ->
                stateMachine.checkTransition(WorkOrderStatus.FORCE_CLOSED, WorkOrderStatus.PENDING_VERIFY)
            );
            assertEquals(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("待派发 -> 处理中（非法，需先派发）")
        void testPendingDispatchToInProgress() {
            BusinessException ex = assertThrows(BusinessException.class, () ->
                stateMachine.checkTransition(WorkOrderStatus.PENDING_DISPATCH, WorkOrderStatus.IN_PROGRESS)
            );
            assertEquals(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("待派发 -> 已完成（非法）")
        void testPendingDispatchToCompleted() {
            BusinessException ex = assertThrows(BusinessException.class, () ->
                stateMachine.checkTransition(WorkOrderStatus.PENDING_DISPATCH, WorkOrderStatus.COMPLETED)
            );
            assertEquals(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("处理中 -> 已取消（非法，需先转派）")
        void testInProgressToCancelled() {
            BusinessException ex = assertThrows(BusinessException.class, () ->
                stateMachine.checkTransition(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.CANCELLED)
            );
            assertEquals(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("待验收 -> 待派发（非法）")
        void testPendingVerifyToPendingDispatch() {
            BusinessException ex = assertThrows(BusinessException.class, () ->
                stateMachine.checkTransition(WorkOrderStatus.PENDING_VERIFY, WorkOrderStatus.PENDING_DISPATCH)
            );
            assertEquals(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("待验收 -> 已取消（非法）")
        void testPendingVerifyToCancelled() {
            BusinessException ex = assertThrows(BusinessException.class, () ->
                stateMachine.checkTransition(WorkOrderStatus.PENDING_VERIFY, WorkOrderStatus.CANCELLED)
            );
            assertEquals(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), ex.getCode());
        }
    }
}