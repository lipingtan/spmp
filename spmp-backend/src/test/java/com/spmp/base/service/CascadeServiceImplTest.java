package com.spmp.base.service;

import com.spmp.base.domain.vo.CascadeVO;
import com.spmp.base.repository.BuildingMapper;
import com.spmp.base.repository.CommunityMapper;
import com.spmp.base.repository.DistrictMapper;
import com.spmp.base.repository.HouseMapper;
import com.spmp.base.repository.UnitMapper;
import com.spmp.base.service.impl.CascadeServiceImpl;
import com.spmp.common.util.RedisUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("级联查询服务测试")
class CascadeServiceImplTest {

    @Mock
    private RedisUtils redisUtils;
    @Mock
    private DistrictMapper districtMapper;
    @Mock
    private CommunityMapper communityMapper;
    @Mock
    private BuildingMapper buildingMapper;
    @Mock
    private UnitMapper unitMapper;
    @Mock
    private HouseMapper houseMapper;

    @InjectMocks
    private CascadeServiceImpl cascadeService;

    @Test
    @DisplayName("缓存命中时直接返回缓存结果")
    void shouldReturnCachedCascadeDataWhenCacheHit() {
        List<CascadeVO> cached = Collections.singletonList(new CascadeVO());
        when(redisUtils.get(eq("spmp:base:cascade:COMMUNITY:1"), eq(List.class))).thenReturn(cached);

        List<CascadeVO> result = cascadeService.getCascadeData("COMMUNITY", 1L);

        assertEquals(1, result.size());
        verify(communityMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("未知级别返回空列表")
    void shouldReturnEmptyListWhenLevelUnknown() {
        when(redisUtils.get(eq("spmp:base:cascade:UNKNOWN:root"), eq(List.class))).thenReturn(null);

        List<CascadeVO> result = cascadeService.getCascadeData("UNKNOWN", null);

        assertTrue(result.isEmpty());
        verify(redisUtils, never()).set(any(), any(), anyLong(), any());
    }
}
