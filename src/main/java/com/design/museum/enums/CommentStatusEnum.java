package com.design.museum.enums;

public enum CommentStatusEnum {
    HIDDEN("隐藏",0),
    DISPLAY("展示",1);
    private String text;
    private int value;
    CommentStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public int getValue(){
        return value;
    }
}
