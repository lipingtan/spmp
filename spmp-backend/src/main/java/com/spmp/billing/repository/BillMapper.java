package com.spmp.billing.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.spmp.billing.domain.dto.BillQueryDTO;
import com.spmp.billing.domain.dto.StatisticsQueryDTO;
import com.spmp.billing.domain.entity.BillDO;
import com.spmp.billing.domain.vo.BillListVO;
import com.spmp.billing.domain.vo.TrendDataVO;
import com.spmp.common.security.DataPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 账单 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface BillMapper extends BaseMapper<BillDO> {

    /**
     * 分页查询账单列表（PC端，数据权限过滤）。
     */
    @DataPermission(communityField = "b.community_id")
    IPage<BillListVO> selectBillPage(IPage<?> page, @Param("query") BillQueryDTO queryDTO);

    /**
     * 统计应收金额。
     */
    @DataPermission(communityField = "b.community_id", buildingField = "b.building_id")
    BigDecimal sumReceivable(@Param("query") StatisticsQueryDTO queryDTO);

    /**
     * 统计实收金额。
     */
    @DataPermission(communityField = "b.community_id", buildingField = "b.building_id")
    BigDecimal sumReceived(@Param("query") StatisticsQueryDTO queryDTO);

    /**
     * 统计逾期金额。
     */
    @DataPermission(communityField = "b.community_id", buildingField = "b.building_id")
    BigDecimal sumOverdue(@Param("query") StatisticsQueryDTO queryDTO);

    /**
     * 统计逾期户数。
     */
    @DataPermission(communityField = "b.community_id", buildingField = "b.building_id")
    int countOverdue(@Param("query") StatisticsQueryDTO queryDTO);

    /**
     * 统计减免金额。
     */
    @DataPermission(communityField = "b.community_id", buildingField = "b.building_id")
    BigDecimal sumReduce(@Param("query") StatisticsQueryDTO queryDTO);

    /**
     * 按月统计趋势数据。
     */
    @DataPermission(communityField = "b.community_id", buildingField = "b.building_id")
    List<TrendDataVO> selectMonthlyTrend(@Param("query") StatisticsQueryDTO queryDTO);

    /**
     * 按业主查询账单列表（对外API）。
     */
    List<BillDO> selectByOwnerId(@Param("ownerId") Long ownerId);

    /**
     * 统计业主逾期账单数（对外API）。
     */
    int countOverdueByOwnerId(@Param("ownerId") Long ownerId);

    /**
     * 按小区和月份统计缴费汇总（对外API）。
     */
    Map<String, Object> sumPaidByCommunityAndMonth(@Param("communityId") Long communityId,
                                                     @Param("month") String month);
}
