package com.design.museum.aop;

import java.lang.annotation.*;

/**
 * 消息通知注解
 * 用于在方法执行成功后自动发送站内消息
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Notice {

    /**
     * 消息分类：1预约 2公告 3活动 4讲座 5系统
     */
    int category() default 1;

    /**
     * 消息标题
     */
    String title() default "系统通知";

    /**
     * 消息内容模板（支持 SpEL 表达式）
     * 示例：预约成功：#{#result.visitDate}，票号：#{#result.ticketCode}
     */
    String message();
}
