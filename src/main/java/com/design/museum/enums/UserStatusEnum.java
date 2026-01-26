package com.design.museum.enums;

public enum UserStatusEnum {
    NORMAL("正常",0),
    BANED("封禁",1);
    private String text;
    private Integer value;
    UserStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public Integer getValue() {
        return value;
    }
}
