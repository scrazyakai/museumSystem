package com.design.museum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
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
 * @since 2026-02-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("visit_day_quota")
public class VisitDayQuota implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 参观日期(唯一)
     */
    private LocalDate visitDate;

    /**
     * 当日总可预约名额
     */
    private Integer capacity;


    /**
     * 1可预约 0停用
     */
    private Integer status;

    /**
     * 0正常 1删除
     */
    private Integer deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
