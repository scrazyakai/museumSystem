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
 * @since 2026-01-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("exhibit_comment")
public class ExhibitComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 被评论的展品/展览ID（exhibit_item.id）
     */
    private Long itemId;

    /**
     * 评论用户
     */
    private Long userId;

    private String content;

    /**
     * 1展示 0隐藏(管理员可审核)
     */
    private Integer status;

    private LocalDateTime createdAt;


}
