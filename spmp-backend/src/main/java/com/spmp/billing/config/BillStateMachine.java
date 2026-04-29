package com.spmp.billing.config;

import com.spmp.billing.constant.BillStatus;
import com.spmp.billing.constant.BillingErrorCode;
import com.spmp.common.exception.BusinessException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 账单状态流转校验（与 {@link BillStatus} 及业务约束对齐的允许边集合）。
 */
public class BillStateMachine {

    private static final Set<String> ALLOWED_EDGES;

    static {
        Set<String> edges = new HashSet<>();
        add(edges, BillStatus.UNPAID, BillStatus.PAYING);
        add(edges, BillStatus.UNPAID, BillStatus.OVERDUE);
        add(edges, BillStatus.UNPAID, BillStatus.CANCELLED);
        add(edges, BillStatus.UNPAID, BillStatus.REDUCED);
        add(edges, BillStatus.PAYING, BillStatus.PAID);
        add(edges, BillStatus.PAYING, BillStatus.UNPAID);
        add(edges, BillStatus.OVERDUE, BillStatus.PAYING);
        add(edges, BillStatus.OVERDUE, BillStatus.PAID);
        add(edges, BillStatus.OVERDUE, BillStatus.CANCELLED);
        add(edges, BillStatus.OVERDUE, BillStatus.REDUCED);
        add(edges, BillStatus.PAID, BillStatus.REFUNDED);
        ALLOWED_EDGES = Collections.unmodifiableSet(edges);
    }

    private static void add(Set<String> edges, BillStatus from, BillStatus to) {
        edges.add(edgeKey(from, to));
    }

    private static String edgeKey(BillStatus from, BillStatus to) {
        return from.getCode() + "->" + to.getCode();
    }

    /**
     * 是否允许从 {@code from} 流转到 {@code to}；同态（from == to）视为允许（无变更）。
     */
    public boolean canTransit(BillStatus from, BillStatus to) {
        if (from == null || to == null) {
            return false;
        }
        if (from == to) {
            return true;
        }
        return ALLOWED_EDGES.contains(edgeKey(from, to));
    }

    /**
     * 校验状态流转，非法则抛出 {@link BusinessException}。
     */
    public void checkTransition(BillStatus from, BillStatus to) {
        if (!canTransit(from, to)) {
            throw new BusinessException(BillingErrorCode.INVALID_STATUS_TRANSITION.getCode(),
                    BillingErrorCode.INVALID_STATUS_TRANSITION.getMessage());
        }
    }
}
