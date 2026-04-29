package com.spmp.billing.service.impl;

import com.spmp.billing.domain.dto.StatisticsQueryDTO;
import com.spmp.billing.domain.vo.StatisticsVO;
import com.spmp.billing.domain.vo.TrendDataVO;
import com.spmp.billing.repository.BillMapper;
import com.spmp.billing.service.BillStatisticsService;
import com.spmp.common.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 收费统计服务实现。
 */
@Service
@RequiredArgsConstructor
public class BillStatisticsServiceImpl implements BillStatisticsService {

    private final BillMapper billMapper;
    private final RedisUtils redisUtils;

    @Override
    public StatisticsVO getStatistics(StatisticsQueryDTO queryDTO) {
        String cacheKey = "billing:statistics:" + md5(buildCacheRaw(queryDTO));
        StatisticsVO cached = redisUtils.get(cacheKey, StatisticsVO.class);
        if (cached != null) {
            return cached;
        }
        StatisticsVO result = new StatisticsVO();
        BigDecimal receivable = nvl(billMapper.sumReceivable(queryDTO));
        BigDecimal received = nvl(billMapper.sumReceived(queryDTO));
        result.setTotalReceivable(receivable);
        result.setTotalReceived(received);
        result.setCollectionRate(receivable.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : received.multiply(BigDecimal.valueOf(100))
                .divide(receivable, 2, BigDecimal.ROUND_HALF_UP));
        result.setOverdueAmount(nvl(billMapper.sumOverdue(queryDTO)));
        result.setOverdueCount(billMapper.countOverdue(queryDTO));
        result.setReduceAmount(nvl(billMapper.sumReduce(queryDTO)));
        List<TrendDataVO> trends = billMapper.selectMonthlyTrend(queryDTO);
        result.setTrends(trends == null ? Collections.emptyList() : trends);
        // 统计数据需尽量贴近实时，避免新增账单后长时间看不到趋势变化。
        redisUtils.set(cacheKey, result, 30, TimeUnit.SECONDS);
        return result;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String buildCacheRaw(StatisticsQueryDTO queryDTO) {
        if (queryDTO == null) {
            return "empty";
        }
        return String.join("|",
                String.valueOf(queryDTO.getTimeRange()),
                String.valueOf(queryDTO.getStartDate()),
                String.valueOf(queryDTO.getEndDate()),
                String.valueOf(queryDTO.getCommunityId()),
                String.valueOf(queryDTO.getBuildingId()),
                String.valueOf(queryDTO.getFeeType()));
    }

    private String md5(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(raw.hashCode());
        }
    }
}
