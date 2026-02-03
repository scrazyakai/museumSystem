package com.design.museum.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import java.time.LocalDate;

/**
 * 更新库存请求
 */
@Data
public class QuotaUpdateRequest {

    /**
     * 参观日期
     */
    @NotNull(message = "参观日期不能为空")
    private LocalDate visitDate;

    /**
     * 新的容量
     */
    @NotNull(message = "容量不能为空")
    @Min(value = 0, message = "容量不能小于0")
    private Integer capacity;
}
