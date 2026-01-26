package com.design.museum.enums;

public enum UserRoleEnum {
    USER("USER"),
    ADMIN("ADMIN");
    String text;
    UserRoleEnum(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
