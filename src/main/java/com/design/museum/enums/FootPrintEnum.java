package com.design.museum.enums;

public enum FootPrintEnum {
    ALLOW("显示足迹",0),
    DISAALOW("不显示足迹",0);
    private String text;
    private int value;
    FootPrintEnum(String text, int value) {
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
