package com.example.help_bridge.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.Arrays;

@Configuration
public class AsyncExceptionConfig implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AsyncExceptionConfig.class);

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> log.error(
                "Async method {}.{} failed with arguments {}",
                method.getDeclaringClass().getSimpleName(), method.getName(), Arrays.toString(params), ex);
    }
}
