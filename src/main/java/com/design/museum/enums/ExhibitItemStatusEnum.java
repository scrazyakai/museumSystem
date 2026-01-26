package com.design.museum.enums;

public enum ExhibitItemStatusEnum {
    DISPLAY("展示",0),
    DOWN("下架",1);
    private String text;
    private int value;
    ExhibitItemStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
