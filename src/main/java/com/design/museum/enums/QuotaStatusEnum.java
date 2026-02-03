package com.design.museum.enums;

public enum QuotaStatusEnum {
    NORMAL("正常",1),
    BANED("禁用",0);
    private String text;
    private int value;
    QuotaStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
