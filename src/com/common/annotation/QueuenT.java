package com.common.annotation;

/**
 * Created by Administrator on 2017/10/11.
 */

import org.springframework.stereotype.Component;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
@Component
public @interface QueuenT {
    String name() default "";
}
