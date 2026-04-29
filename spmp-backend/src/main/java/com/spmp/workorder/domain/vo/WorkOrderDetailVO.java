package com.spmp.workorder.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工单详情 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class WorkOrderDetailVO {

    private Long id;

    private String orderNo;

    private String orderType;

    private String addressType;

    private Long communityId;

    private String communityName;

    private Long houseId;

    private String houseName;

    private Long buildingId;

    private String buildingName;

    private Long unitId;

    private String unitName;

    private Long reporterId;

    private String reporterName;

    private String reporterPhone;

    private String description;

    private String status;

    private Long repairUserId;

    private String repairUserName;

    private Integer rejectCount;

    private Integer urgeCount;

    private LocalDateTime lastUrgeTime;

    private LocalDateTime expectedCompleteTime;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualCompleteTime;

    private Integer repairDuration;

    private String cancelReason;

    private LocalDateTime createTime;

    private List<String> reportImages;

    private List<DispatchRecordVO> dispatchRecords;

    private List<RepairMaterialVO> materials;

    private EvaluationVO evaluation;

    private List<WorkOrderLogVO> logs;
}
