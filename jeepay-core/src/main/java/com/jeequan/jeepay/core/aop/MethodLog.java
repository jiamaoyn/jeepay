package com.jeequan.jeepay.core.aop;

import java.lang.annotation.*;

/**
 * 方法级日志切面注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MethodLog {
    String remark() default "";
}
