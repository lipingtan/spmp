package com.spmp.billing.config;

import com.spmp.common.exception.BusinessException;
import com.spmp.billing.constant.BillStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("账单状态机测试")
class BillStateMachineTest {

    private final BillStateMachine billStateMachine = new BillStateMachine();

    @Test
    @DisplayName("合法状态流转应通过校验")
    void shouldAllowValidTransition() {
        assertDoesNotThrow(() -> billStateMachine.checkTransition(BillStatus.UNPAID, BillStatus.PAYING));
        assertTrue(billStateMachine.canTransit(BillStatus.PAID, BillStatus.REFUNDED));
    }

    @Test
    @DisplayName("非法状态流转应抛出异常")
    void shouldRejectInvalidTransition() {
        assertThrows(BusinessException.class,
                () -> billStateMachine.checkTransition(BillStatus.REFUNDED, BillStatus.UNPAID));
        assertFalse(billStateMachine.canTransit(BillStatus.CANCELLED, BillStatus.PAID));
    }
}
