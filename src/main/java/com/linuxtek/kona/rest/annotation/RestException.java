/*
 * Copyright 2012 LINUXTEK, Inc.
 */
package com.linuxtek.kona.rest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Target(value={ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RestException {
    int status();
    int code() default -1;
    String clientMsg() default "_clientMsg";
    String devMsg() default "_devMsg";
    String infoUrl() default "_infoUrl";
}
