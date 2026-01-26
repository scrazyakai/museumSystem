package com.design.museum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2026-01-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("visit_booking")
public class VisitBooking implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 1个人 2团体
     */
    private Integer bookingType;

    /**
     * 个人预约的发起人；团体可为空
     */
    private Long userId;

    private Long timeslotId;

    /**
     * 团体：学校/旅行社
     */
    private String orgName;

    private String contactName;

    private String contactPhone;

    private Integer peopleCount;

    /**
     * 团体成员列表：[{name,idNo,remark}]
     */
    private String membersJson;

    /**
     * 电子票/核验码
     */
    private String bookingCode;

    /**
     * 1已预约 2已取消 3已改签 4已核验 5待审核 6已拒绝
     */
    private Integer status;

    private String auditReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
