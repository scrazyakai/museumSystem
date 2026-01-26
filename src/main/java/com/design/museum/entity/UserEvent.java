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
@TableName("user_event")
public class UserEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * FAVORITE/UNFAVORITE/FOOTPRINT/CHECKIN/MESSAGE/PAGE_VIEW/CLICK/NOTICE
     */
    private String eventType;

    /**
     * EXHIBITION/ARTIFACT/ACTIVITY/LECTURE/... 与 content.type 对齐
     */
    private String targetType;

    /**
     * 指向 content.id 或其他
     */
    private Long targetId;

    /**
     * 扩展数据：留言内容、打卡照片、停留时长等
     */
    private String dataJson;

    private LocalDateTime createdAt;


}
