package com.design.museum.enums;

public enum PushMessageEnum {
    ALLOW("允许发送消息",0),
    DISALLOW("不允许发送消息",1);
    private String text;
    private int value;
    PushMessageEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public int getValue() {
        return value;
    }
}
