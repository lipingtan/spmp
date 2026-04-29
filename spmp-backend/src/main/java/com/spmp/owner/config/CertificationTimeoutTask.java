package com.spmp.owner.config;

import com.spmp.owner.domain.entity.CertificationDO;
import com.spmp.owner.service.CertificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 认证超时提醒定时任务。
 * <p>
 * 每天凌晨 2:00 扫描超过 7 天未审批的认证申请，记录日志并发送通知。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CertificationTimeoutTask {

    private final CertificationService certificationService;

    /**
     * 每天凌晨 2:00 执行，扫描超时未审批的认证申请。
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkTimeoutCertifications() {
        List<CertificationDO> timeoutList = certificationService.listTimeoutPending(7);
        if (timeoutList.isEmpty()) {
            log.info("认证超时检查完成，无超时申请");
            return;
        }

        log.info("发现 {} 条超时未审批的认证申请", timeoutList.size());

        // 逐条记录日志，后续可扩展为按管辖楼栋分组发送系统内通知
        for (CertificationDO cert : timeoutList) {
            log.info("认证申请 {} 已超时 7 天未审批，ownerId={}, houseId={}，通知管理员",
                    cert.getId(), cert.getOwnerId(), cert.getHouseId());
        }
    }
}
