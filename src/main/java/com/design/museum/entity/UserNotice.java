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
 * @since 2026-01-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_notice")
public class UserNotice implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接收用户
     */
    private Long userId;

    /**
     * 1预约 2公告 3活动 4讲座 5系统
     */
    private Integer category;

    private String title;

    private String content;

    /**
     * 0未读 1已读
     */
    private Integer readFlag;

    /**
     * 0正常 1删除
     */
    private Integer deleted;

    private LocalDateTime createdAt;


}
