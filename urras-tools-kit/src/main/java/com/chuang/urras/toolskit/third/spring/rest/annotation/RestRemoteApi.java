package com.chuang.urras.toolskit.third.spring.rest.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface RestRemoteApi {
    String domain();
}
