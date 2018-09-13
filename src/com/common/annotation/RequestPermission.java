package com.common.annotation;

/**
 * Created by Administrator on 2017/10/11.
 */

import org.apache.shiro.authz.annotation.Logical;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Component
/*@RequestPermission(value = { "product_create", "product_edit" }, logical = Logical.OR)*/
public @interface RequestPermission {
    String[] value();
    Logical logical() default Logical.AND;
}
