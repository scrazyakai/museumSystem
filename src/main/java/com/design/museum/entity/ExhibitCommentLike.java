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
 * @since 2026-02-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("exhibit_comment_like")
public class ExhibitCommentLike implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 评论ID（exhibit_comment.id）
     */
    private Long commentId;

    /**
     * 点赞用户（sys_user.id）
     */
    private Long userId;

    /**
     * 0点赞 1取消点赞(逻辑删除)
     */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
