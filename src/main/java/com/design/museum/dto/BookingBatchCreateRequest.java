package com.design.museum.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 批量创建预约请求
 */
@Data
public class BookingBatchCreateRequest {

    /**
     * 参观日期
     */
    @NotNull(message = "参观日期不能为空")
    private LocalDate visitDate;

    /**
     * 用户ID列表
     */
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;
}
