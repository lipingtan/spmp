package com.spmp.owner.config;

import com.spmp.common.exception.BusinessException;
import com.spmp.common.util.SecurityUtils;
import com.spmp.owner.api.OwnerApi;
import com.spmp.owner.constant.OwnerErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * 业主认证状态校验 AOP 切面。
 * <p>
 * 拦截标注了 {@link RequireCertified} 的方法，从 SecurityContext 获取当前 owner_id，
 * 调用 {@link OwnerApi#checkOwnerCertified(Long)} 检查认证状态。
 * 未认证时抛出 {@link BusinessException}(OWNER_NOT_CERTIFIED)。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RequireCertifiedAspect {

    private final OwnerApi ownerApi;

    /**
     * 在标注了 @RequireCertified 的方法执行前校验业主认证状态。
     *
     * @param joinPoint        切入点
     * @param requireCertified 注解实例
     */
    @Before("@annotation(requireCertified)")
    public void checkCertified(JoinPoint joinPoint, RequireCertified requireCertified) {
        Long ownerId = SecurityUtils.getCurrentOwnerId();
        if (ownerId == null) {
            log.warn("@RequireCertified 校验失败：未找到业主信息，method={}",
                    joinPoint.getSignature().toShortString());
            throw new BusinessException(OwnerErrorCode.OWNER_NOT_FOUND.getCode(), "未找到业主信息");
        }

        boolean certified = ownerApi.checkOwnerCertified(ownerId);
        if (!certified) {
            log.info("@RequireCertified 校验未通过：业主未认证，ownerId={}, method={}",
                    ownerId, joinPoint.getSignature().toShortString());
            throw new BusinessException(OwnerErrorCode.OWNER_NOT_CERTIFIED.getCode(),
                    requireCertified.message());
        }
    }
}
