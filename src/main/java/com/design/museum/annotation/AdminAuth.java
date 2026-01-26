package com.design.museum.annotation;

import java.lang.annotation.*;

/**
 * 管理员权限注解
 * 标注该注解的接口需要管理员权限才能访问
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AdminAuth {
}
