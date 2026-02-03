package com.design.museum.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 查询库存请求
 */
@Data
public class QuotaQueryRequest {

    /**
     * 参观日期
     */
    @NotNull(message = "参观日期不能为空")
    private LocalDate visitDate;
}
