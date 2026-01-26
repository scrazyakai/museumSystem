package com.design.museum.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 添加评论请求
 */
@Data
public class CommentAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;
}