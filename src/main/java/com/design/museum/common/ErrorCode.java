package com.design.museum.common;

/**
 * 自定义错误码
 */
public enum ErrorCode {

    SUCCESS(200, "ok"),
    PARAMS_ERROR(400, "请求参数错误"),
    NOT_LOGIN_ERROR(401, "未登录"),
    NO_AUTH_ERROR(402, "无权限"),
    NOT_FOUND_ERROR(404, "请求数据不存在"),
    FORBIDDEN_ERROR(403, "禁止访问"),
    SYSTEM_ERROR(500, "系统内部异常"),
    OPERATION_ERROR(501, "操作失败"),
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_PASSWORD_NOT_MATCH(1004, "两次密码不一致"),
    USER_ALREADY_EXIST(1005, "用户名已存在"),

    // 预约相关错误码
    USER_NOT_REALNAME(2001, "请先绑定实名信息"),
    BOOKING_NOT_FOUND(2002, "预约记录不存在"),
    BOOKING_STATUS_ERROR(2003, "预约状态不允许该操作"),
    BOOKING_EXPIRED(2004, "预约已过期"),
    BOOKING_DATE_INVALID(2005, "预约日期无效"),
    BOOKING_ALREADY_EXISTS(2006, "该日期已有预约"),
    BOOKING_RESCHEDULE_LIMIT(2007, "今日已改签一次，请明日再试"),
    BOOKING_NOT_ALLOW_RESCHEDULE(2008, "该预约不允许改签");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
