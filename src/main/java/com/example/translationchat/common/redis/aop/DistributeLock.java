package com.example.translationchat.common.redis.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributeLock {
    String key();

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    long waitTime() default 5L; // 락을 획득하기 위한 대기 시간

    long leaseTime() default 3L; // 락을 임대하는 시간
}
