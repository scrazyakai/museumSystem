package com.design.museum.enums;

public enum CommentLikeStatusEnum {
    LIKE("点赞",1),
    CANCEL_LIKE("未点赞",0);
    private String text;
    private int value;
    CommentLikeStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
